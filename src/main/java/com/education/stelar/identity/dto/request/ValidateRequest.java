package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ValidateRequest(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
