package com.education.stelar.identity.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.identity.entity.Tenant;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String logoUrl,
        boolean active,
        UUID ownerId,
        Instant createdAt
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getDescription(),
                tenant.getLogoUrl(),
                tenant.isActive(),
                tenant.getOwnerId(),
                tenant.getCreatedAt()
        );
    }
}
