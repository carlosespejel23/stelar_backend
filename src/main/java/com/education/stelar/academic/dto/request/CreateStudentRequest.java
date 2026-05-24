package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStudentRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String lastName,

        @Size(max = 50, message = "El código no puede superar 50 caracteres")
        String studentCode,

        @Email(message = "Formato de email inválido")
        String email
) {
}
