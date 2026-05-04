package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateTenantRequest(

        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description,

        @Size(max = 500, message = "La URL no puede superar 500 caracteres")
        String logoUrl
) {
}
