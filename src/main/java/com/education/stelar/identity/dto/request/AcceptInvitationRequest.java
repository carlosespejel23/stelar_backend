package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(

        @NotBlank(message = "El token de invitación es obligatorio")
        String token
) {
}
