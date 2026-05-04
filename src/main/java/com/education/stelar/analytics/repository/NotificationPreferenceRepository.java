package com.education.stelar.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.analytics.entity.EmailFrequency;
import com.education.stelar.analytics.entity.NotificationPreference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    Optional<NotificationPreference> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    List<NotificationPreference> findAllByTenantId(UUID tenantId);

    List<NotificationPreference> findAllByEmailEnabledTrueAndEmailFrequency(EmailFrequency emailFrequency);
}
