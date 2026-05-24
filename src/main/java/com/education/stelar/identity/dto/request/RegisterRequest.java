package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request de registro de nueva escuela + usuario administrador.
 * El slug del tenant se genera automáticamente en el backend — el frontend NO lo envía.
 */
public record RegisterRequest(

        @NotBlank(message = "El nombre de la escuela es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String schoolName,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String lastName,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Size(max = 200, message = "El email no puede superar 200 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        // Opcional — descripción de la institución
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String description,

        // Opcional — SEMESTER | TRIMESTER | QUARTER. Si no se envía, se usa SEMESTER.
        String periodType
) {
}
