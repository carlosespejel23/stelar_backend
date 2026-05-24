package com.education.stelar.identity.dto.response;

import java.util.UUID;

import com.education.stelar.identity.entity.Tenant;

public record TenantSummaryResponse(
        UUID id,
        String name,
        String slug,
        String logoUrl
) {
    public static TenantSummaryResponse from(Tenant tenant) {
        return new TenantSummaryResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getLogoUrl()
        );
    }
}
