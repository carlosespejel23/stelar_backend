package com.education.stelar.analytics.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.education.stelar.analytics.entity.InAppNotification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, UUID> {

    Page<InAppNotification> findAllByUserIdAndTenantIdOrderByCreatedAtDesc(
            UUID userId, UUID tenantId, Pageable pageable);

    long countByUserIdAndTenantIdAndReadFalse(UUID userId, UUID tenantId);

    Optional<InAppNotification> findByIdAndUserId(UUID id, UUID userId);

    List<InAppNotification> findAllByUserIdAndTenantIdAndReadFalse(UUID userId, UUID tenantId);

    @Modifying
    @Query("UPDATE InAppNotification n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.tenantId = :tenantId AND n.read = false")
    int markAllReadByUserAndTenant(@Param("userId") UUID userId, @Param("tenantId") UUID tenantId);
}
