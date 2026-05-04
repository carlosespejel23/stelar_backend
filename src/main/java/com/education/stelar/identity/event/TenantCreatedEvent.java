package com.education.stelar.identity.event;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.kernel.event.DomainEvent;

public record TenantCreatedEvent(
        UUID eventId,
        Instant occurredOn,
        UUID tenantId,
        String tenantSlug,
        String description,
        // Stored as String to avoid Identity → Academic cross-module dependency.
        // Valid values: SEMESTER, TRIMESTER, QUARTER. Defaults to SEMESTER if null/invalid.
        String periodType
) implements DomainEvent {

    /** Convenience constructor used by AuthService during registration. */
    public TenantCreatedEvent(UUID tenantId, String tenantSlug, String description, String periodType) {
        this(UUID.randomUUID(), Instant.now(), tenantId, tenantSlug, description, periodType);
    }

    @Override
    public String eventType() {
        return "identity.tenant.created";
    }
}
