package com.education.stelar.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.entity.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findFirstByUserIdAndUsedFalseAndExpiresAtAfter(UUID userId, Instant now);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.userId = :userId AND t.used = false")
    void deleteAllByUserIdAndUsedFalse(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :cutoff")
    void deleteAllExpiredBefore(Instant cutoff);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.used = true OR t.expiresAt < :cutoff")
    int deleteUsedOrExpiredBefore(Instant cutoff);
}
