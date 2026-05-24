package com.education.stelar.academic.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.academic.entity.Group;

public record GroupResponse(
        UUID id,
        UUID tenantId,
        String name,
        String level,
        String schoolYear,
        UUID teacherId,
        boolean active,
        Instant createdAt
) {
    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getTenantId(),
                group.getName(),
                group.getLevel(),
                group.getSchoolYear(),
                group.getTeacherId(),
                group.isActive(),
                group.getCreatedAt()
        );
    }
}
