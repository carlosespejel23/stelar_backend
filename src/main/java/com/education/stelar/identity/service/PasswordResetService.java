package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.entity.PasswordResetToken;
import com.education.stelar.identity.entity.User;
import com.education.stelar.identity.repository.PasswordResetTokenRepository;
import com.education.stelar.identity.repository.UserRepository;
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
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Transactional
    public void initiatePasswordReset(String email) {
        var userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.info("Password reset solicitado para email no registrado: {} (tenant: {})", email);
            return;
        }

        User user = userOpt.get();

        tokenRepository.deleteAllByUserIdAndUsedFalse(user.getId());

        long expirationSeconds = appProperties.getEmail().getResetTokenExpiration();
        PasswordResetToken token = PasswordResetToken.create(
                UUID.randomUUID().toString(),
                user.getId(),
                user.getEmail(),
                Instant.now().plusSeconds(expirationSeconds)
        );
        tokenRepository.save(token);

        String link = appProperties.getFrontendUrl() + "/auth/jwt/reset-password?token=" + token.getToken();
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto">
                  <h2 style="color:#4F46E5">Restablece tu contraseña</h2>
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Recibimos una solicitud para restablecer tu contraseña. Haz clic en el botón para continuar:</p>
                  <div style="text-align:center;margin:32px 0">
                    <a href="%s"
                       style="background:#4F46E5;color:#fff;padding:14px 28px;border-radius:6px;text-decoration:none;font-weight:bold">
                      Restablecer contraseña
                    </a>
                  </div>
                  <p style="color:#666;font-size:13px">Este enlace expirará en 24 horas. Si no solicitaste este cambio, ignora este mensaje.</p>
                </div>
                """.formatted(user.getFullName(), link);

        emailService.send(user.getEmail(), "Restablece tu contraseña en Stellar", html);
        log.info("Token de reset enviado al usuario: {}", user.getId());
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "El token no es válido"));

        if (!token.isValid()) {
            if (token.isExpired()) {
                throw new BusinessException("TOKEN_EXPIRED", "El enlace de restablecimiento ha expirado. Solicita uno nuevo.");
            }
            throw new BusinessException("TOKEN_USED", "Este enlace ya fue utilizado.");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", token.getUserId()));

        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        token.markUsed();
        tokenRepository.save(token);

        log.info("Contraseña restablecida para usuario: {}", user.getId());
    }
}
