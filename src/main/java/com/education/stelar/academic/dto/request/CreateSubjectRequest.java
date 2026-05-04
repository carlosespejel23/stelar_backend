package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateSubjectRequest(

        @NotBlank(message = "El nombre de la materia es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String description,

        UUID teacherId
) {
}
