package com.education.stelar.identity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.identity.dto.request.*;
import com.education.stelar.identity.dto.response.AuthResponse;
import com.education.stelar.identity.dto.response.SessionResponse;
import com.education.stelar.identity.dto.response.TenantSummaryResponse;
import com.education.stelar.identity.dto.response.UserResponse;
import com.education.stelar.identity.dto.response.ValidateResponse;
import com.education.stelar.identity.service.AuthService;
import com.education.stelar.identity.service.EmailVerificationService;
import com.education.stelar.identity.service.PasswordResetService;
import com.education.stelar.identity.service.UserDetailsImpl;
import com.education.stelar.identity.service.UserService;
import com.education.stelar.kernel.security.CurrentUser;

import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login, refresh y gestión de contraseña")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nueva escuela y usuario administrador")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String slug = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Registro exitoso. Revisa tu correo para verificar tu cuenta.",
                        "slug", slug
                ));
    }

    @PostMapping("/validate")
    @Operation(
            summary = "Fase 1 del login — validar credenciales",
            description = "Verifica email y contraseña globalmente y devuelve las escuelas donde el usuario tiene cuenta activa."
    )
    public ResponseEntity<ValidateResponse> validate(@Valid @RequestBody ValidateRequest request) {
        return ResponseEntity.ok(authService.validate(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Fase 2 del login — autenticación completa",
            description = "Autentica al usuario en la escuela seleccionada y devuelve access token + refresh token."
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar tokens usando refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión (revoca refresh token + blacklistea access token)")
    public ResponseEntity<Map<String, String>> logout(
            @CurrentUser UserDetailsImpl currentUser,
            HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String accessToken = StringUtils.hasText(header) && header.startsWith("Bearer ")
                ? header.substring(7) : null;
        authService.logout(accessToken, currentUser.getId(), currentUser.getTenantId());
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente."));
    }

    @GetMapping("/session")
    @Operation(
            summary = "Sesión completa del usuario autenticado",
            description = "Retorna perfil + permisos + tenants en una sola llamada. " +
                    "Reemplaza los 3 endpoints separados (me, me/permissions, me/tenants) post-login."
    )
    public ResponseEntity<SessionResponse> getSession(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.ok(authService.getSessionInfo(currentUser));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener información del usuario autenticado")
    public ResponseEntity<UserResponse> me(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.ok(userService.findById(currentUser.getId()));
    }

    @GetMapping("/me/permissions")
    @Operation(summary = "Obtener permisos del usuario autenticado")
    public ResponseEntity<List<String>> myPermissions(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.ok(authService.getMyPermissions(currentUser.getId()));
    }

    @GetMapping("/me/tenants")
    @Operation(summary = "Obtener escuelas del usuario autenticado")
    public ResponseEntity<List<TenantSummaryResponse>> myTenants(@CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.ok(authService.getMyTenants(currentUser.getEmail()));
    }

    @PostMapping("/switch-tenant")
    @Operation(summary = "Cambiar de escuela activa")
    public ResponseEntity<AuthResponse> switchTenant(
            @Valid @RequestBody SwitchTenantRequest request,
            @CurrentUser UserDetailsImpl currentUser) {
        return ResponseEntity.ok(authService.switchTenant(request, currentUser.getEmail()));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verificar email con token")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.token());
        return ResponseEntity.ok(Map.of("message", "Email verificado correctamente. Ya puedes iniciar sesión."));
    }

    @PostMapping("/verify-email/resend")
    @Operation(summary = "Reenviar email de verificación")
    public ResponseEntity<Map<String, String>> resendVerification(
            @Valid @RequestBody ForgotPasswordRequest request) {
        emailVerificationService.resendVerificationEmail(request.email());
        return ResponseEntity.ok(Map.of("message",
                "Si tu cuenta existe y el email no está verificado, recibirás un nuevo correo."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar restablecimiento de contraseña")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.initiatePasswordReset(request.email());
        return ResponseEntity.ok(Map.of("message",
                "Si tu cuenta existe, recibirás un correo con instrucciones para restablecer tu contraseña."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente."));
    }
}
