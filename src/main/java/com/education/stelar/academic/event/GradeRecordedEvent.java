package com.education.stelar.academic.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.education.stelar.kernel.event.DomainEvent;

public record GradeRecordedEvent(
        UUID gradeId,
        UUID studentId,
        UUID subjectId,
        UUID enrollmentId,
        UUID evaluationPeriodId,
        BigDecimal score,
        UUID tenantId,
        Instant occurredAt
) implements DomainEvent {

    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredOn() {
        return occurredAt;
    }

    @Override
    public String eventType() {
        return "grade.recorded";
    }
}
