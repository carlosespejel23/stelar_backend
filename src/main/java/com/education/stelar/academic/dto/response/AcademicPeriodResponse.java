package com.education.stelar.academic.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.education.stelar.academic.entity.AcademicPeriod;
import com.education.stelar.academic.entity.PeriodType;

public record AcademicPeriodResponse(
        UUID id,
        UUID tenantId,
        String name,
        PeriodType periodType,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        int totalWeeks,
        int currentWeek,
        List<EvaluationPeriodResponse> evaluationPeriods,
        Instant createdAt
) {
    public static AcademicPeriodResponse from(AcademicPeriod ap) {
        return new AcademicPeriodResponse(
                ap.getId(),
                ap.getTenantId(),
                ap.getName(),
                ap.getPeriodType(),
                ap.getStartDate(),
                ap.getEndDate(),
                ap.isActive(),
                ap.getTotalWeeks(),
                ap.getCurrentWeek(),
                ap.getEvaluationPeriods().stream()
                        .map(EvaluationPeriodResponse::from)
                        .toList(),
                ap.getCreatedAt()
        );
    }
}
