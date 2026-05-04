package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnrollStudentRequest(

        @NotNull(message = "El ID del estudiante es obligatorio")
        UUID studentId,

        @NotNull(message = "El ID del grupo es obligatorio")
        UUID groupId,

        @NotNull(message = "El ID de la materia es obligatorio")
        UUID subjectId,

        @NotBlank(message = "El ciclo escolar es obligatorio")
        String schoolYear
) {
}
