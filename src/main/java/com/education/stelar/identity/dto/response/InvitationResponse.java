package com.education.stelar.identity.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.identity.entity.Invitation;
import com.education.stelar.identity.entity.InvitationStatus;
import com.education.stelar.identity.entity.InvitationType;

public record InvitationResponse(
        UUID id,
        UUID tenantId,
        String invitedEmail,
        InvitationType type,
        InvitationStatus status,
        String token,
        UUID roleId,
        UUID invitedByUserId,
        Instant expiresAt,
        Instant createdAt
) {
    public static InvitationResponse from(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getTenantId(),
                invitation.getInvitedEmail(),
                invitation.getType(),
                invitation.getStatus(),
                invitation.getToken(),
                invitation.getRoleId(),
                invitation.getInvitedByUserId(),
                invitation.getExpiresAt(),
                invitation.getCreatedAt()
        );
    }
}
