package com.education.stelar.identity.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.identity.entity.Profession;
import com.education.stelar.identity.entity.User;
import com.education.stelar.identity.entity.UserTenant;

public record UserResponse(
        UUID id,
        UUID tenantId,
        String firstName,
        String lastName,
        String email,
        String profilePictureUrl,
        boolean emailVerified,
        boolean active,
        String roleName,
        UUID roleId,
        Profession profession,
        Instant createdAt
) {
    public static UserResponse from(User user, UserTenant userTenant) {
        return new UserResponse(
                user.getId(),
                userTenant.getTenantId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfilePictureUrl(),
                user.isEmailVerified(),
                userTenant.isActive(),
                userTenant.getRoleName(),
                userTenant.getRoleId(),
                user.getProfession(),
                user.getCreatedAt()
        );
    }
}
