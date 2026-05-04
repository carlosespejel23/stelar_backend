package com.education.stelar.academic.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.education.stelar.academic.entity.AttendanceStatus;
import com.education.stelar.kernel.event.DomainEvent;

public record AttendanceRecordedEvent(
        UUID attendanceId,
        UUID studentId,
        UUID enrollmentId,
        AttendanceStatus status,
        LocalDate date,
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
        return "attendance.recorded";
    }
}
