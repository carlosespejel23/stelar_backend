package com.education.stelar.analytics.dto.request;

import jakarta.validation.constraints.Size;

public record DismissAlertRequest(

        @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
        String reason
) {
}
