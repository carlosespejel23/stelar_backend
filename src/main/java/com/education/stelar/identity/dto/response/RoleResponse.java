package com.education.stelar.identity.dto.response;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.education.stelar.identity.entity.Role;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        boolean systemRole,
        boolean active,
        List<PermissionResponse> permissions
) {
    public static RoleResponse from(Role role) {
        List<PermissionResponse> perms = role.getPermissions().stream()
                .filter(p -> p.isActive())
                .map(PermissionResponse::from)
                .sorted(Comparator.comparing(PermissionResponse::code))
                .toList();
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isSystemRole(),
                role.isActive(),
                perms
        );
    }
}
