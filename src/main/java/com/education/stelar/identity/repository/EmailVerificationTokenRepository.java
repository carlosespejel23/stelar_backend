package com.education.stelar.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.entity.EmailVerificationToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findFirstByUserIdAndUsedFalseAndExpiresAtAfter(UUID userId, Instant now);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken t WHERE t.userId = :userId AND t.used = false")
    void deleteAllByUserIdAndUsedFalse(UUID userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :cutoff")
    void deleteAllExpiredBefore(Instant cutoff);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken t WHERE t.used = true OR t.expiresAt < :cutoff")
    int deleteUsedOrExpiredBefore(Instant cutoff);
}
