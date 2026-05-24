package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.education.stelar.identity.entity.EmailVerificationToken;
import com.education.stelar.identity.entity.User;
import com.education.stelar.identity.event.UserRegisteredEvent;
import com.education.stelar.identity.repository.EmailVerificationTokenRepository;
import com.education.stelar.identity.repository.UserRepository;
import com.education.stelar.identity.repository.UserTenantRepository;
import com.education.stelar.kernel.config.AppProperties;
import com.education.stelar.kernel.email.EmailService;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserTenantRepository userTenantRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

    @Transactional
    public void sendVerificationEmail(User user) {
        tokenRepository.deleteAllByUserIdAndUsedFalse(user.getId());

        long expirationSeconds = appProperties.getEmail().getVerificationTokenExpiration();
        Instant expiresAt = Instant.now().plusSeconds(expirationSeconds);

        EmailVerificationToken token = EmailVerificationToken.create(
                UUID.randomUUID().toString(),
                user.getId(),
                user.getEmail(),
                expiresAt
        );
        tokenRepository.save(token);

        String link = appProperties.getFrontendUrl() + "/auth/jwt/verify-email?token=" + token.getToken();
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
                  <h2 style="color:#4F46E5">Bienvenido a Stellar</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Gracias por registrarte. Por favor verifica tu correo electrónico haciendo clic en el botón:</p>
                  <div style="text-align:center;margin:32px 0">
                    <a href="%s"
                       style="background:#4F46E5;color:#fff;padding:14px 28px;border-radius:6px;text-decoration:none;font-weight:bold">
                      Verificar mi correo
                    </a>
                  </div>
                  <p style="color:#666;font-size:13px">Este enlace expirará en 24 horas. Si no creaste una cuenta, ignora este mensaje.</p>
                </div>
                """.formatted(user.getFullName(), link);

        emailService.send(user.getEmail(), "Verifica tu cuenta en Stellar", html);
        log.info("Token de verificación enviado al usuario: {}", user.getId());
    }

    @Transactional
    public User verifyEmail(String tokenValue) {
        EmailVerificationToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "El token de verificación no es válido"));

        if (!token.isValid()) {
            if (token.isExpired()) {
                throw new BusinessException("TOKEN_EXPIRED", "El token de verificación ha expirado. Solicita uno nuevo.");
            }
            throw new BusinessException("TOKEN_USED", "Este token ya fue utilizado.");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", token.getUserId()));

        user.verifyEmail();
        userRepository.save(user);

        // Activate all UserTenants for this user (email is now verified globally)
        userTenantRepository.findAllByUser_Id(user.getId())
                .forEach(ut -> {
                    ut.activate();
                    userTenantRepository.save(ut);
                });

        token.markUsed();
        tokenRepository.save(token);

        log.info("Email verificado para usuario: {}", user.getId());
        return user;
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.isEmailVerified()) {
            throw new BusinessException("EMAIL_ALREADY_VERIFIED", "El email ya fue verificado.");
        }

        boolean hasValidToken = tokenRepository
                .findFirstByUserIdAndUsedFalseAndExpiresAtAfter(user.getId(), Instant.now())
                .isPresent();

        if (hasValidToken) {
            throw new BusinessException("TOKEN_STILL_VALID",
                    "Ya existe un token de verificación activo. Revisa tu correo o espera a que expire.");
        }

        sendVerificationEmail(user);
    }

    /**
     * Escucha el UserRegisteredEvent y envía el email de verificación de forma
     * asíncrona DESPUÉS de que la transacción de registro haya sido commiteada.
     *
     * Esto elimina ~300-500ms del tiempo de respuesta de /auth/register:
     * el endpoint retorna inmediatamente tras el commit y el email se envía en background.
     *
     * REQUIRES_NEW abre una transacción propia (el commit ya ocurrió — no hay transacción activa).
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserRegistered(UserRegisteredEvent event) {
        User user = userRepository.findById(event.userId()).orElse(null);
        if (user == null || user.isEmailVerified()) return;

        try {
            sendVerificationEmail(user);
        } catch (Exception e) {
            log.error("Error enviando email de verificación al usuario {}: {}", event.userId(), e.getMessage());
        }
    }
}
