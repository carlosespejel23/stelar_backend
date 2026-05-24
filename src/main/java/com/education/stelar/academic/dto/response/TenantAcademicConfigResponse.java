package com.education.stelar.academic.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.education.stelar.academic.entity.PeriodType;
import com.education.stelar.academic.entity.TenantAcademicConfig;

public record TenantAcademicConfigResponse(
        UUID id,
        UUID tenantId,
        PeriodType periodType,
        BigDecimal gradingScaleMin,
        BigDecimal gradingScaleMax,
        BigDecimal passingGrade,
        Integer attendanceMinimumPct
) {
    public static TenantAcademicConfigResponse from(TenantAcademicConfig config) {
        return new TenantAcademicConfigResponse(
                config.getId(),
                config.getTenantId(),
                config.getPeriodType(),
                config.getGradingScaleMin(),
                config.getGradingScaleMax(),
                config.getPassingGrade(),
                config.getAttendanceMinimumPct()
        );
    }
}
