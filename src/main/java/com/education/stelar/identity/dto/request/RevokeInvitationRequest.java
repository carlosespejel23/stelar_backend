package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RevokeInvitationRequest(

        @NotNull(message = "El ID de la invitación es obligatorio")
        UUID invitationId
) {
}
