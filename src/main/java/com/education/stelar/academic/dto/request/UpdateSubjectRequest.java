package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateSubjectRequest(

        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String description
) {
}
