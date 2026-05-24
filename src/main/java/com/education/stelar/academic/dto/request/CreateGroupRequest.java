package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateGroupRequest(

        @NotBlank(message = "El nombre del grupo es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Size(max = 50, message = "El nivel no puede superar 50 caracteres")
        String level,

        @Size(max = 20, message = "El ciclo escolar no puede superar 20 caracteres")
        String schoolYear,

        UUID teacherId
) {
}
