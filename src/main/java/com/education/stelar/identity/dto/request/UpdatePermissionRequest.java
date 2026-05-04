package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.Size;

public record UpdatePermissionRequest(

        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String description,

        @Size(max = 50, message = "La categoría no puede superar 50 caracteres")
        String category,

        @Size(max = 50, message = "La acción no puede superar 50 caracteres")
        String action,

        @Size(max = 100, message = "El recurso no puede superar 100 caracteres")
        String resource
) {
}
