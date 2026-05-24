package com.education.stelar.analytics.dto.response;

import java.util.UUID;

import com.education.stelar.analytics.entity.RiskWeights;

public record RiskWeightsResponse(
        UUID id,
        UUID tenantId,
        double lowAverage,
        double negativeTrend,
        double lowAttendance,
        double consecutiveAbsences,
        double failedSubjects,
        double temporalUrgency
) {
    public static RiskWeightsResponse from(RiskWeights w) {
        return new RiskWeightsResponse(
                w.getId(), w.getTenantId(),
                w.getLowAverage(), w.getNegativeTrend(), w.getLowAttendance(),
                w.getConsecutiveAbsences(), w.getFailedSubjects(), w.getTemporalUrgency()
        );
    }
}
