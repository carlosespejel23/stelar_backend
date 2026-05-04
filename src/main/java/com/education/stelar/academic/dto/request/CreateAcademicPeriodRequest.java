package com.education.stelar.academic.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.education.stelar.academic.entity.PeriodType;

public record CreateAcademicPeriodRequest(

        @NotBlank(message = "El nombre del ciclo es obligatorio")
        String name,

        @NotNull(message = "El tipo de período es obligatorio")
        PeriodType periodType,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate endDate,

        @NotEmpty(message = "Debe definir al menos un período de evaluación")
        @Valid
        List<EvaluationPeriodRequest> evaluationPeriods
) {
    public record EvaluationPeriodRequest(

            @NotBlank(message = "El nombre del período de evaluación es obligatorio")
            String name,

            @NotNull(message = "El peso es obligatorio")
            @DecimalMin(value = "0.01", message = "El peso mínimo es 0.01")
            @DecimalMax(value = "100.00", message = "El peso máximo es 100")
            BigDecimal weight,

            @NotNull(message = "El orden es obligatorio")
            @Min(value = 1, message = "El orden mínimo es 1")
            Integer sequenceOrder,

            LocalDate startDate,
            LocalDate endDate
    ) {
    }
}
