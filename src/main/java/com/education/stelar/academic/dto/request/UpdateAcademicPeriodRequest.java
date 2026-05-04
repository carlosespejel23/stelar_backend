package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateAcademicPeriodRequest(

        @NotBlank(message = "El nombre del ciclo es obligatorio")
        String name,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate startDate,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate endDate
) {
}
