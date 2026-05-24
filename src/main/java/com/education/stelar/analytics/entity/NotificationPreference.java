package com.education.stelar.analytics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_notification_pref_tenant_user",
                columnNames = {"tenant_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level_threshold", nullable = false, length = 20)
    private RiskLevel alertLevelThreshold = RiskLevel.MEDIUM;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_frequency", nullable = false, length = 20)
    private EmailFrequency emailFrequency = EmailFrequency.DAILY_DIGEST;

    public static NotificationPreference createDefault(UUID userId) {
        NotificationPreference pref = new NotificationPreference();
        pref.userId = userId;
        pref.alertLevelThreshold = RiskLevel.MEDIUM;
        pref.emailEnabled = true;
        pref.inAppEnabled = true;
        pref.emailFrequency = EmailFrequency.DAILY_DIGEST;
        return pref;
    }

    public void update(RiskLevel alertLevelThreshold, boolean emailEnabled,
                       boolean inAppEnabled, EmailFrequency emailFrequency) {
        this.alertLevelThreshold = alertLevelThreshold;
        this.emailEnabled = emailEnabled;
        this.inAppEnabled = inAppEnabled;
        this.emailFrequency = emailFrequency;
    }

    public boolean shouldNotifyForLevel(RiskLevel level) {
        return level.ordinal() >= this.alertLevelThreshold.ordinal();
    }
}
