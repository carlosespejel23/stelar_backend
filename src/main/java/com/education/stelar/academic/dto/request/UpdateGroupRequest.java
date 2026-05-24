package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateGroupRequest(

        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Size(max = 50, message = "El nivel no puede superar 50 caracteres")
        String level
) {
}
