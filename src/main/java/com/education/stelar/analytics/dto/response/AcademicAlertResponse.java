package com.education.stelar.analytics.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.analytics.entity.AcademicAlert;
import com.education.stelar.analytics.entity.AlertStatus;
import com.education.stelar.analytics.entity.RiskLevel;

public record AcademicAlertResponse(
        UUID id,
        UUID tenantId,
        UUID studentId,
        UUID academicPeriodId,
        RiskLevel riskLevel,
        int riskScore,
        String factorScores,
        AlertStatus status,
        UUID dismissedBy,
        Instant dismissedAt,
        String dismissReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static AcademicAlertResponse from(AcademicAlert alert) {
        return new AcademicAlertResponse(
                alert.getId(), alert.getTenantId(),
                alert.getStudentId(), alert.getAcademicPeriodId(),
                alert.getRiskLevel(), alert.getRiskScore(),
                alert.getFactorScores(), alert.getStatus(),
                alert.getDismissedBy(), alert.getDismissedAt(),
                alert.getDismissReason(),
                alert.getCreatedAt(), alert.getUpdatedAt()
        );
    }
}
