package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InviteExternalRequest(

        @NotBlank(message = "El email del invitado es obligatorio")
        @Email(message = "Formato de email inválido")
        String invitedEmail,

        @NotNull(message = "El rol a asignar es obligatorio")
        UUID roleId,

        String message
) {
}
