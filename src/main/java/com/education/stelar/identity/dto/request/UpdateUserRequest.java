package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.Size;

import java.util.UUID;

import com.education.stelar.identity.entity.Profession;

public record UpdateUserRequest(

        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String lastName,

        @Size(max = 500, message = "La URL no puede superar 500 caracteres")
        String profilePictureUrl,

        UUID roleId,

        Profession profession
) {
}
