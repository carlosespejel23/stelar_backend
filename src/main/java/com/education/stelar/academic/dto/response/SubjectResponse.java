package com.education.stelar.academic.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.academic.entity.Subject;

public record SubjectResponse(
        UUID id,
        UUID tenantId,
        String name,
        String description,
        UUID teacherId,
        boolean active,
        Instant createdAt
) {
    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getTenantId(),
                subject.getName(),
                subject.getDescription(),
                subject.getTeacherId(),
                subject.isActive(),
                subject.getCreatedAt()
        );
    }
}
