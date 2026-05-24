package com.education.stelar.identity.dto.response;

import java.util.UUID;

import com.education.stelar.identity.entity.Permission;

public record PermissionResponse(
        UUID id,
        String code,
        String description,
        String category,
        String action,
        String resource,
        String type,
        boolean active
) {
    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getCode(),
                permission.getDescription(),
                permission.getCategory(),
                permission.getAction(),
                permission.getResource(),
                permission.getType(),
                permission.isActive()
        );
    }
}
