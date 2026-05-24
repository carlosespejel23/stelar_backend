package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.repository.EmailVerificationTokenRepository;
import com.education.stelar.identity.repository.PasswordResetTokenRepository;
import com.education.stelar.identity.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Limpieza periódica de tokens inútiles en la base de datos.
 * Se ejecuta cada 6 horas para evitar acumulación infinita de filas.
 *
 * Criterios de eliminación:
 *   - RefreshToken:            revocado=true  O  expirado
 *   - EmailVerificationToken:  usado=true     O  expirado
 *   - PasswordResetToken:      usado=true     O  expirado
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;

    @Scheduled(fixedRate = 6, timeUnit = TimeUnit.HOURS)
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();

        int refreshDeleted      = refreshTokenRepository.deleteRevokedOrExpiredBefore(now);
        int verificationDeleted = verificationTokenRepository.deleteUsedOrExpiredBefore(now);
        int resetDeleted        = resetTokenRepository.deleteUsedOrExpiredBefore(now);

        if (refreshDeleted + verificationDeleted + resetDeleted > 0) {
            log.info("Token cleanup: {} refresh, {} verificación, {} reset eliminados",
                    refreshDeleted, verificationDeleted, resetDeleted);
        }
    }
}
