package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateStudentRequest(

        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String firstName,

        @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
        String lastName,

        @Email(message = "Formato de email inválido")
        String email
) {
}
