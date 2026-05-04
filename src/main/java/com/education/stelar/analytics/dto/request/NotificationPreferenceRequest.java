package com.education.stelar.analytics.dto.request;

import com.education.stelar.analytics.entity.EmailFrequency;
import com.education.stelar.analytics.entity.RiskLevel;

import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceRequest(

        @NotNull(message = "El nivel mínimo de alerta es obligatorio")
        RiskLevel alertLevelThreshold,

        @NotNull(message = "Indica si el email está habilitado")
        Boolean emailEnabled,

        @NotNull(message = "Indica si las notificaciones in-app están habilitadas")
        Boolean inAppEnabled,

        @NotNull(message = "La frecuencia de email es obligatoria")
        EmailFrequency emailFrequency
) {
}
