package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

import com.education.stelar.identity.entity.Profession;

public record CreateUserRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Size(max = 200)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100)
        String password,

        @NotNull(message = "El rol es obligatorio")
        UUID roleId,

        // Optional — shown in greeting/UI to personalize experience
        Profession profession
) {
}
