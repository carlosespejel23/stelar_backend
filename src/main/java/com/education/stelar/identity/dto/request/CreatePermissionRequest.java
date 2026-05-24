package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(

        @NotBlank(message = "El código del permiso es obligatorio")
        @Size(max = 150, message = "El código no puede superar 150 caracteres")
        @Pattern(regexp = "^[A-Z0-9_:.]+$", message = "El código solo puede contener letras mayúsculas, números, guiones bajos, puntos y dos puntos")
        String code,

        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String description,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 50, message = "La categoría no puede superar 50 caracteres")
        String category,

        @Size(max = 50, message = "La acción no puede superar 50 caracteres")
        String action,

        @Size(max = 100, message = "El recurso no puede superar 100 caracteres")
        String resource,

        @Pattern(regexp = "BACKEND|FRONTEND|FULL_STACK", message = "El tipo debe ser BACKEND, FRONTEND o FULL_STACK")
        String type
) {
}
