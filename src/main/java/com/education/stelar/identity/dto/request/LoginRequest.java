package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password,

        @NotBlank(message = "El slug de la escuela es obligatorio")
        String tenantSlug,

        /**
         * Opcional. Token de corta vida recibido en /validate que ya verificó
         * las credenciales con BCrypt. Si es válido y coincide con el email,
         * este endpoint omite el segundo BCrypt. Si es null o expiró, se hace
         * la autenticación completa (password obligatorio).
         */
        String preAuthToken
) {
}
