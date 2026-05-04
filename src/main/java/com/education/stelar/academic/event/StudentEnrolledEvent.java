package com.education.stelar.academic.event;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.kernel.event.DomainEvent;

public record StudentEnrolledEvent(
        UUID enrollmentId,
        UUID studentId,
        UUID groupId,
        UUID subjectId,
        UUID tenantId
) implements DomainEvent {

    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredOn() {
        return Instant.now();
    }

    @Override
    public String eventType() {
        return "student.enrolled";
    }
}
