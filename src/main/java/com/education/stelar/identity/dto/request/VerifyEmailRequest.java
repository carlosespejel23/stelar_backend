package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @NotBlank(message = "El token de verificación es obligatorio")
        String token
) {
}
