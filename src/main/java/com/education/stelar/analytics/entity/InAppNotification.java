package com.education.stelar.analytics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "in_app_notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InAppNotification extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel severity;

    @Column(name = "alert_id")
    private UUID alertId;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "read_at")
    private Instant readAt;

    public static InAppNotification create(UUID userId, String title, String message,
                                           RiskLevel severity, UUID alertId) {
        InAppNotification n = new InAppNotification();
        n.userId = userId;
        n.title = title;
        n.message = message;
        n.severity = severity;
        n.alertId = alertId;
        return n;
    }

    public void markRead() {
        this.read = true;
        this.readAt = Instant.now();
    }
}
