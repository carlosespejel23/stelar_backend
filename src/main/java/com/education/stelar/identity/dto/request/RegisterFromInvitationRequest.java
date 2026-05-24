package com.education.stelar.identity.dto.request;

import com.education.stelar.identity.entity.Profession;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterFromInvitationRequest(

        @NotBlank(message = "El token de invitación es obligatorio")
        String token,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String lastName,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        // Optional — shown in greeting/UI to personalize experience
        Profession profession
) {
}
