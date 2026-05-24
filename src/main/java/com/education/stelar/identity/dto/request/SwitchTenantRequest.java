package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SwitchTenantRequest(

        @NotBlank(message = "El slug de la escuela es obligatorio")
        String tenantSlug
) {
}
