package com.education.stelar.kernel.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();

    Instant occurredOn();

    String eventType();
}
