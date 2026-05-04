package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

import com.education.stelar.academic.entity.PeriodType;

public record TenantAcademicConfigRequest(

        @NotNull(message = "El tipo de período es obligatorio")
        PeriodType periodType,

        @NotNull(message = "La calificación mínima es obligatoria")
        @DecimalMin(value = "0", message = "La calificación mínima no puede ser negativa")
        BigDecimal gradingScaleMin,

        @NotNull(message = "La calificación máxima es obligatoria")
        @DecimalMin(value = "1", message = "La calificación máxima debe ser al menos 1")
        BigDecimal gradingScaleMax,

        @NotNull(message = "La calificación mínima aprobatoria es obligatoria")
        @DecimalMin(value = "0", message = "La calificación aprobatoria no puede ser negativa")
        BigDecimal passingGrade,

        @NotNull(message = "El porcentaje mínimo de asistencia es obligatorio")
        @Min(value = 0, message = "El porcentaje mínimo es 0")
        @Max(value = 100, message = "El porcentaje máximo es 100")
        Integer attendanceMinimumPct
) {
}
