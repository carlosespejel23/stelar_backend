package com.education.stelar.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.entity.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUserIdAndTenantIdAndRevoked(UUID userId, UUID tenantId, boolean revoked);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId AND rt.tenantId = :tenantId AND rt.revoked = false")
    void revokeAllByUserIdAndTenantId(UUID userId, UUID tenantId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :cutoff")
    void deleteAllExpiredBefore(Instant cutoff);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true OR rt.expiresAt < :cutoff")
    int deleteRevokedOrExpiredBefore(Instant cutoff);
}
