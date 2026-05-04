package com.education.stelar.academic.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BatchGradeRequest(

        @NotNull(message = "El ID del período de evaluación es obligatorio")
        UUID evaluationPeriodId,

        @NotNull(message = "El ID de la materia es obligatorio")
        UUID subjectId,

        @NotNull(message = "El ID del grupo es obligatorio")
        UUID groupId,

        @NotEmpty(message = "Debe incluir al menos una calificación")
        @Valid
        List<StudentGradeEntry> grades
) {
    public record StudentGradeEntry(

            @NotNull(message = "El ID del estudiante es obligatorio")
            UUID studentId,

            @NotNull(message = "La calificación es obligatoria")
            @DecimalMin(value = "0.00", message = "La calificación mínima es 0")
            @DecimalMax(value = "100.00", message = "La calificación máxima es 100")
            BigDecimal score,

            @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
            String remarks
    ) {
    }
}
