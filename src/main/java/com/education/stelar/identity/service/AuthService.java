package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.dto.request.*;
import com.education.stelar.identity.dto.response.*;
import com.education.stelar.identity.entity.*;
import com.education.stelar.identity.event.TenantCreatedEvent;
import com.education.stelar.identity.event.UserRegisteredEvent;
import com.education.stelar.identity.repository.*;
import com.education.stelar.kernel.config.AppProperties;
import com.education.stelar.kernel.event.EventPublisher;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;
import com.education.stelar.kernel.security.JwtTokenProvider;
import com.education.stelar.kernel.security.PreAuthTokenService;
import com.education.stelar.kernel.security.TokenBlacklistService;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    public static final String ROLE_ADMIN   = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_VIEWER  = "VIEWER";

    /** Permisos operativos del rol TEACHER. */
    private static final Set<String> TEACHER_PERMISSIONS = Set.of(
            "STUDENTS_VIEW", "STUDENTS_CREATE", "STUDENTS_EDIT",
            "GROUPS_VIEW",   "GROUPS_CREATE",   "GROUPS_EDIT",
            "SUBJECTS_VIEW", "SUBJECTS_CREATE", "SUBJECTS_EDIT",
            "GRADES_VIEW",   "GRADES_CREATE",   "GRADES_EDIT",
            "ATTENDANCE_VIEW", "ATTENDANCE_CREATE", "ATTENDANCE_EDIT",
            "ALERTS_VIEW",
            "REPORTS_VIEW",  "REPORTS_GENERATE"
    );

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final PreAuthTokenService preAuthTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final EventPublisher eventPublisher;

    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // ----------------------------------------------------------------
    // Register — crea tenant + roles del sistema (con permisos) + admin
    // ----------------------------------------------------------------

    @Transactional
    public String register(RegisterRequest request) {
        // 1. Generar slug único en el backend
        String slug = generateSlug(request.schoolName());

        // 2. Crear tenant
        Tenant tenant = Tenant.create(request.schoolName(), slug);
        if (request.description() != null && !request.description().isBlank()) {
            tenant.update(null, request.description(), null);
        }
        tenantRepository.save(tenant);
        TenantContext.setCurrentTenant(tenant.getId());

        // Publicar evento — el módulo Academic escucha para inicializar TenantAcademicConfig
        eventPublisher.publish(new TenantCreatedEvent(
                tenant.getId(), tenant.getSlug(), request.description(), request.periodType()));

        // 3. Crear roles del sistema con permisos para este tenant
        Role adminRole = createSystemRoles();

        // 4. Crear usuario global
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("USER_EMAIL_EXISTS",
                    "Ya existe un usuario con ese email. Usa el sistema de invitaciones para unirte a esta escuela.");
        }

        User admin = userRepository.save(User.create(
                request.firstName(), request.lastName(),
                request.email(), passwordEncoder.encode(request.password()),
                null  // profession — configurable via user settings after registration
        ));

        // 5. Crear relación admin-tenant (inactivo hasta verificar email)
        userTenantRepository.save(UserTenant.create(admin, adminRole));

        // 6. Enlazar owner en el tenant
        tenant.assignOwner(admin.getId());
        tenantRepository.save(tenant);

        // 7. Publicar evento — EmailVerificationService lo escucha con @TransactionalEventListener
        //    y envía el email DESPUÉS del commit (async), sin bloquear esta respuesta.
        eventPublisher.publish(new UserRegisteredEvent(admin.getId(), tenant.getId(), admin.getEmail()));
        log.info("Tenant '{}' registrado con admin: {}", slug, admin.getEmail());

        return slug;
    }

    // ----------------------------------------------------------------
    // Login
    // ----------------------------------------------------------------

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Tenant tenant = tenantRepository.findBySlug(request.tenantSlug())
                .filter(Tenant::isActive)
                .orElseThrow(() -> new BusinessException("TENANT_NOT_FOUND",
                        "Escuela no encontrada o inactiva: " + request.tenantSlug()));

        TenantContext.setCurrentTenant(tenant.getId());

        UserDetailsImpl userDetails = resolveUserDetails(request, tenant.getId());

        if (!userDetails.isEmailVerified()) {
            throw new BusinessException("EMAIL_NOT_VERIFIED",
                    "Debes verificar tu correo electrónico antes de iniciar sesión.");
        }

        return buildAuthResponse(userDetails, tenant.getId());
    }

    /**
     * Ruta rápida: si viene un preAuthToken válido (emitido por /validate),
     * se omite BCrypt — el costo ya se pagó en /validate.
     * Ruta lenta (fallback): autenticación completa con BCrypt.
     */
    private UserDetailsImpl resolveUserDetails(LoginRequest request, UUID tenantId) {
        String preAuthEmail = preAuthTokenService.consumeAndGetEmail(request.preAuthToken());

        if (preAuthEmail != null && preAuthEmail.equalsIgnoreCase(request.email())) {
            // Fast path — BCrypt ya fue verificado en /validate
            UserTenant userTenant = userTenantRepository
                    .findByUser_EmailAndTenantId(request.email(), tenantId)
                    .orElseThrow(() -> new BusinessException("USER_NOT_IN_TENANT",
                            "El usuario no tiene acceso a esta escuela."));
            return UserDetailsImpl.build(userTenant.getUser(), userTenant);
        }

        // Fallback — autenticación completa (clientes que llaman /login directamente)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    // ----------------------------------------------------------------
    // Refresh token
    // ----------------------------------------------------------------

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN", "Refresh token inválido."));

        if (!refreshToken.isActive()) {
            throw new BusinessException("REFRESH_TOKEN_EXPIRED", "El refresh token ha expirado o fue revocado.");
        }

        UserTenant userTenant = userTenantRepository
                .findByUser_IdAndTenantId(refreshToken.getUserId(), refreshToken.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", refreshToken.getUserId()));

        TenantContext.setCurrentTenant(refreshToken.getTenantId());
        UserDetailsImpl userDetails = UserDetailsImpl.build(userTenant.getUser(), userTenant);
        return buildAuthResponse(userDetails, refreshToken.getTenantId());
    }

    // ----------------------------------------------------------------
    // Logout
    // ----------------------------------------------------------------

    @Transactional
    public void logout(String accessToken, UUID userId, UUID tenantId) {
        // 1. Revocar refresh tokens en DB
        refreshTokenRepository.revokeAllByUserIdAndTenantId(userId, tenantId);

        // 2. Agregar access token a la blacklist de Redis (con TTL = tiempo restante)
        if (accessToken != null) {
            try {
                String jti = jwtTokenProvider.getJtiFromToken(accessToken);
                Date expiration = jwtTokenProvider.getExpirationFromToken(accessToken);
                if (jti != null && expiration != null) {
                    long remainingMs = expiration.getTime() - System.currentTimeMillis();
                    tokenBlacklistService.blacklist(jti, remainingMs);
                }
            } catch (Exception e) {
                log.warn("No se pudo blacklistear el access token al logout: {}", e.getMessage());
            }
        }

        log.info("Sesión cerrada para usuario: {} en tenant: {}", userId, tenantId);
    }

    // ----------------------------------------------------------------
    // Validate — fase 1 del login
    // ----------------------------------------------------------------

    public ValidateResponse validate(ValidateRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("INVALID_CREDENTIALS", "Email o contraseña incorrectos."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("INVALID_CREDENTIALS", "Email o contraseña incorrectos.");
        }

        List<TenantSummaryResponse> tenants = resolveActiveTenants(request.email(), user.isEmailVerified());

        // Emitir pre-auth token (TTL 30s) para que /login evite re-verificar BCrypt.
        // Graceful: si Redis no está disponible, preAuthToken = null y /login usa fallback.
        String preAuthToken = preAuthTokenService.create(request.email());

        return new ValidateResponse(user.getFirstName(), user.getLastName(), user.getEmail(),
                tenants, preAuthToken);
    }

    // ----------------------------------------------------------------
    // Me / tenants del usuario autenticado
    // ----------------------------------------------------------------

    public List<TenantSummaryResponse> getMyTenants(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isEmailVerified()) return List.of();

        return resolveActiveTenants(email, true);
    }

    // ----------------------------------------------------------------
    // Permissions del usuario autenticado
    // ----------------------------------------------------------------

    public List<String> getMyPermissions(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        // EntityGraph carga UserTenant + User + Role + permissions en 1 query JOIN
        UserTenant userTenant = userTenantRepository.findByUser_IdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));

        if (userTenant.getRole() == null) return List.of();

        // El role ya está cargado por EntityGraph — no se necesita roleRepository.findById()
        return userTenant.getRole().getPermissions().stream()
                .filter(Permission::isActive)
                .map(Permission::getCode)
                .sorted()
                .toList();
    }

    // ----------------------------------------------------------------
    // Session info — consolida me + permissions + tenants en 1 llamada
    // ----------------------------------------------------------------

    public SessionResponse getSessionInfo(UserDetailsImpl currentUser) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // 1 query JOIN: UserTenant + User + Role + permissions
        UserTenant userTenant = userTenantRepository.findByUser_IdAndTenantId(currentUser.getId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", currentUser.getId()));

        UserResponse userResponse = UserResponse.from(userTenant.getUser(), userTenant);

        List<String> permissions = userTenant.getRole() != null
                ? userTenant.getRole().getPermissions().stream()
                        .filter(Permission::isActive)
                        .map(Permission::getCode)
                        .sorted()
                        .toList()
                : List.of();

        List<TenantSummaryResponse> tenants = resolveActiveTenants(currentUser.getEmail(), true);

        return new SessionResponse(userResponse, permissions, tenants);
    }

    // ----------------------------------------------------------------
    // Switch tenant
    // ----------------------------------------------------------------

    @Transactional
    public AuthResponse switchTenant(SwitchTenantRequest request, String email) {
        Tenant newTenant = tenantRepository.findBySlug(request.tenantSlug())
                .filter(Tenant::isActive)
                .orElseThrow(() -> new BusinessException("TENANT_NOT_FOUND",
                        "Escuela no encontrada o inactiva: " + request.tenantSlug()));

        UserTenant userTenant = userTenantRepository
                .findByUser_EmailAndTenantId(email, newTenant.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_IN_TENANT",
                        "No tienes una cuenta en la escuela: " + request.tenantSlug()));

        if (!userTenant.isActive()) {
            throw new BusinessException("USER_INACTIVE", "Tu cuenta en esa escuela está desactivada.");
        }
        if (!userTenant.getUser().isEmailVerified()) {
            throw new BusinessException("EMAIL_NOT_VERIFIED",
                    "Debes verificar tu correo en esa escuela antes de acceder.");
        }

        TenantContext.setCurrentTenant(newTenant.getId());
        UserDetailsImpl userDetails = UserDetailsImpl.build(userTenant.getUser(), userTenant);
        return buildAuthResponse(userDetails, newTenant.getId());
    }

    // ----------------------------------------------------------------
    // Internal helpers
    // ----------------------------------------------------------------

    /**
     * Genera un slug URL-friendly a partir del nombre del tenant.
     * Normaliza acentos, elimina caracteres especiales, agrega sufijo de 6 chars
     * para unicidad. Formato: "preparatoria-juarez-a7k2m9".
     */
    private String generateSlug(String tenantName) {
        String base = tenantName.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("ñ", "n")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (base.isBlank()) base = "escuela";

        String slug;
        int attempts = 0;
        do {
            // Sufijo de 6 caracteres hex (sin guiones) del UUID — sin dependencias externas
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            slug = base + "-" + suffix;
            attempts++;
        } while (tenantRepository.existsBySlug(slug) && attempts < 10);

        return slug;
    }

    /**
     * Crea los 3 roles del sistema para el tenant actual (TenantContext debe estar seteado).
     * ADMIN recibe todos los permisos; TEACHER recibe permisos operativos; VIEWER solo _VIEW.
     * Retorna el rol ADMIN para asignarlo al usuario que se registra.
     */
    private Role createSystemRoles() {
        List<Permission> allPermissions = permissionRepository.findAllByActive(true);
        Set<Permission> allPermsSet = new HashSet<>(allPermissions);

        // ADMIN: todos los permisos
        Role admin = Role.createSystemRole(ROLE_ADMIN, "Administrador de la plataforma");
        admin.setPermissions(allPermsSet);
        admin = roleRepository.save(admin);

        // TEACHER: permisos operativos definidos en TEACHER_PERMISSIONS
        Set<Permission> teacherPerms = allPermissions.stream()
                .filter(p -> TEACHER_PERMISSIONS.contains(p.getCode()))
                .collect(Collectors.toSet());
        Role teacher = Role.createSystemRole(ROLE_TEACHER, "Docente");
        teacher.setPermissions(teacherPerms);
        roleRepository.save(teacher);

        // VIEWER: solo permisos de lectura (*_VIEW)
        Set<Permission> viewerPerms = allPermissions.stream()
                .filter(p -> p.getCode().endsWith("_VIEW"))
                .collect(Collectors.toSet());
        Role viewer = Role.createSystemRole(ROLE_VIEWER, "Solo lectura");
        viewer.setPermissions(viewerPerms);
        roleRepository.save(viewer);

        return admin;
    }

    /**
     * Carga los tenants activos del usuario en 2 queries (en vez de N+1):
     * 1. findAllByUser_Email → list de UserTenants (columna tenantId, sin lazy loads)
     * 2. tenantRepository.findAllById(ids) → IN query con todos los tenant IDs
     */
    private List<TenantSummaryResponse> resolveActiveTenants(String email, boolean emailVerified) {
        if (!emailVerified) return List.of();

        List<UUID> tenantIds = userTenantRepository.findAllByUser_Email(email).stream()
                .filter(UserTenant::isActive)
                .map(UserTenant::getTenantId)
                .distinct()
                .toList();

        if (tenantIds.isEmpty()) return List.of();

        Map<UUID, Tenant> tenantsById = tenantRepository.findAllById(tenantIds).stream()
                .collect(Collectors.toMap(Tenant::getId, Function.identity()));

        return tenantIds.stream()
                .map(tenantsById::get)
                .filter(t -> t != null && t.isActive())
                .map(TenantSummaryResponse::from)
                .toList();
    }

    private AuthResponse buildAuthResponse(UserDetailsImpl userDetails, UUID tenantId) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList();

        String accessToken = jwtTokenProvider.generateAccessToken(
                userDetails.getEmail(),
                userDetails.getId(),
                tenantId,
                roles
        );

        refreshTokenRepository.revokeAllByUserIdAndTenantId(userDetails.getId(), tenantId);

        long refreshExpMs = appProperties.getJwt().getRefreshTokenExpiration();
        RefreshToken refreshToken = RefreshToken.create(
                UUID.randomUUID().toString(),
                userDetails.getId(),
                tenantId,
                Instant.now().plusMillis(refreshExpMs)
        );
        refreshTokenRepository.save(refreshToken);

        UserTenant userTenant = userTenantRepository
                .findByUser_IdAndTenantId(userDetails.getId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userDetails.getId()));

        return new AuthResponse(accessToken, refreshToken.getToken(),
                UserResponse.from(userTenant.getUser(), userTenant));
    }
}
