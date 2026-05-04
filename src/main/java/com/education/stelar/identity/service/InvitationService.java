package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.dto.request.*;
import com.education.stelar.identity.dto.response.InvitationResponse;
import com.education.stelar.identity.dto.response.UserResponse;
import com.education.stelar.identity.entity.*;
import com.education.stelar.identity.repository.*;
import com.education.stelar.kernel.config.AppProperties;
import com.education.stelar.kernel.email.EmailService;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationService {

    private static final long INVITATION_TTL_SECONDS = 7 * 24 * 60 * 60L; // 7 days

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    // ── List ──────────────────────────────────────────────────────────

    public List<InvitationResponse> findAll() {
        return invitationRepository.findAllByTenantId(TenantContext.getCurrentTenant())
                .stream()
                .map(InvitationResponse::from)
                .toList();
    }

    // ── Invite external (sends email) ────────────────────────────────

    @Transactional
    public InvitationResponse inviteExternal(InviteExternalRequest request, UUID invitedByUserId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        validateNoActiveInvitation(request.invitedEmail(), tenantId);

        Role role = roleRepository.findById(request.roleId())
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Rol", request.roleId()));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        String token = UUID.randomUUID().toString();
        Invitation invitation = Invitation.create(
                request.invitedEmail(), InvitationType.EXTERNAL,
                token, role.getId(), invitedByUserId,
                Instant.now().plusSeconds(INVITATION_TTL_SECONDS)
        );
        invitation = invitationRepository.save(invitation);

        sendInvitationEmail(request.invitedEmail(), tenant.getName(), token, request.message());
        log.info("External invitation sent to {} for tenant {}", request.invitedEmail(), tenantId);

        return InvitationResponse.from(invitation);
    }

    // ── Invite internal (no email) ───────────────────────────────────

    @Transactional
    public InvitationResponse inviteInternal(InviteInternalRequest request, UUID invitedByUserId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        validateNoActiveInvitation(request.invitedEmail(), tenantId);

        Role role = roleRepository.findById(request.roleId())
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Rol", request.roleId()));

        String token = UUID.randomUUID().toString();
        Invitation invitation = Invitation.create(
                request.invitedEmail(), InvitationType.INTERNAL,
                token, role.getId(), invitedByUserId,
                Instant.now().plusSeconds(INVITATION_TTL_SECONDS)
        );

        return InvitationResponse.from(invitationRepository.save(invitation));
    }

    // ── Accept (for INTERNAL invitations — user already exists) ──────

    @Transactional
    public UserResponse accept(AcceptInvitationRequest request) {
        Invitation invitation = findValidInvitation(request.token());

        UUID tenantId = invitation.getTenantId();
        TenantContext.setCurrentTenant(tenantId);

        // User must already exist globally
        User user = userRepository.findByEmail(invitation.getInvitedEmail())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND",
                        "No se encontró un usuario con ese email. Usa el flujo de registro desde invitación."));

        if (userTenantRepository.existsByUser_EmailAndTenantId(invitation.getInvitedEmail(), tenantId)) {
            throw new BusinessException("USER_ALREADY_IN_TENANT",
                    "El usuario ya tiene una cuenta en esta escuela.");
        }

        Role role = resolveRole(invitation.getRoleId(), tenantId);

        // Create active UserTenant (user is already email-verified)
        UserTenant userTenant = userTenantRepository.save(UserTenant.createActive(user, role));

        invitation.accept();
        invitationRepository.save(invitation);

        log.info("Internal invitation accepted: {} joined tenant {}", user.getEmail(), tenantId);
        return UserResponse.from(user, userTenant);
    }

    // ── Register from invitation (for EXTERNAL — new user) ───────────

    @Transactional
    public UserResponse registerFromInvitation(RegisterFromInvitationRequest request) {
        Invitation invitation = findValidInvitation(request.token());

        UUID tenantId = invitation.getTenantId();
        TenantContext.setCurrentTenant(tenantId);

        if (userTenantRepository.existsByUser_EmailAndTenantId(invitation.getInvitedEmail(), tenantId)) {
            throw new BusinessException("USER_ALREADY_IN_TENANT",
                    "El usuario ya tiene una cuenta en esta escuela.");
        }

        if (userRepository.existsByEmail(invitation.getInvitedEmail())) {
            throw new BusinessException("USER_EXISTS_GLOBALLY",
                    "Este email ya está registrado. Si ya tienes cuenta, usa el flujo de aceptar invitación.");
        }

        Role role = resolveRole(invitation.getRoleId(), tenantId);

        User newUser = User.create(
                request.firstName(), request.lastName(),
                invitation.getInvitedEmail(),
                passwordEncoder.encode(request.password()),
                request.profession()
        );
        newUser.verifyEmail(); // skip email verification — invitation is proof of identity
        newUser = userRepository.save(newUser);

        // Create active UserTenant (email pre-verified via invitation)
        UserTenant userTenant = userTenantRepository.save(UserTenant.createActive(newUser, role));

        invitation.accept();
        invitationRepository.save(invitation);

        log.info("User registered from invitation: {} in tenant {}", newUser.getEmail(), tenantId);
        return UserResponse.from(newUser, userTenant);
    }

    // ── Revoke ────────────────────────────────────────────────────────

    @Transactional
    public void revoke(RevokeInvitationRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Invitation invitation = invitationRepository.findById(request.invitationId())
                .filter(inv -> inv.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitación", request.invitationId()));

        if (!invitation.isPending()) {
            throw new BusinessException("INVITATION_NOT_PENDING",
                    "Solo se pueden revocar invitaciones en estado PENDING.");
        }

        invitation.revoke();
        invitationRepository.save(invitation);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Invitation findValidInvitation(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("INVALID_INVITATION_TOKEN",
                        "Token de invitación inválido o no encontrado."));

        if (!invitation.isPending()) {
            throw new BusinessException("INVITATION_ALREADY_USED",
                    "Esta invitación ya fue utilizada o revocada.");
        }
        if (invitation.isExpired()) {
            throw new BusinessException("INVITATION_EXPIRED",
                    "Esta invitación ha expirado.");
        }
        return invitation;
    }

    private void validateNoActiveInvitation(String email, UUID tenantId) {
        invitationRepository.findByInvitedEmailAndTenantIdAndStatus(
                email.toLowerCase().trim(), tenantId, InvitationStatus.PENDING
        ).ifPresent(existing -> {
            throw new BusinessException("INVITATION_ALREADY_PENDING",
                    "Ya existe una invitación pendiente para: " + email);
        });
    }

    private Role resolveRole(UUID roleId, UUID tenantId) {
        if (roleId == null) {
            return roleRepository.findByNameAndTenantId("TEACHER", tenantId)
                    .orElseThrow(() -> new BusinessException("DEFAULT_ROLE_NOT_FOUND",
                            "No se encontró el rol por defecto para este tenant."));
        }
        return roleRepository.findById(roleId)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Rol", roleId));
    }

    private void sendInvitationEmail(String to, String tenantName, String token, String message) {
        String link = appProperties.getFrontendUrl() + "/invitations/accept?token=" + token;
        String body = "<h2>Te han invitado a unirte a " + tenantName + "</h2>"
                + (message != null && !message.isBlank()
                ? "<p><em>" + message + "</em></p>"
                : "")
                + "<p>Haz clic en el siguiente enlace para aceptar la invitación:</p>"
                + "<p><a href=\"" + link + "\">Aceptar invitación</a></p>"
                + "<p>Este enlace expirará en 7 días.</p>";
        emailService.send(to, "Invitación a " + tenantName, body);
    }
}
