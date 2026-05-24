package com.education.stelar.analytics.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.analytics.entity.InAppNotification;
import com.education.stelar.analytics.entity.RiskLevel;

public record InAppNotificationResponse(
        UUID id,
        UUID userId,
        String title,
        String message,
        RiskLevel severity,
        UUID alertId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
    public static InAppNotificationResponse from(InAppNotification n) {
        return new InAppNotificationResponse(
                n.getId(), n.getUserId(),
                n.getTitle(), n.getMessage(),
                n.getSeverity(), n.getAlertId(),
                n.isRead(), n.getReadAt(),
                n.getCreatedAt()
        );
    }
}
