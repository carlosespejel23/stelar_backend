package com.education.stelar.identity.event;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.kernel.event.DomainEvent;

public record UserRegisteredEvent(
        UUID eventId,
        Instant occurredOn,
        UUID userId,
        UUID tenantId,
        String email
) implements DomainEvent {

    public UserRegisteredEvent(UUID userId, UUID tenantId, String email) {
        this(UUID.randomUUID(), Instant.now(), userId, tenantId, email);
    }

    @Override
    public String eventType() {
        return "identity.user.registered";
    }
}
