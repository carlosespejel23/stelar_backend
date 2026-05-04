package com.education.stelar.academic.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.academic.entity.Enrollment;

public record EnrollmentResponse(
        UUID id,
        UUID tenantId,
        UUID studentId,
        UUID groupId,
        UUID subjectId,
        String schoolYear,
        boolean active,
        Instant createdAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getTenantId(),
                enrollment.getStudentId(),
                enrollment.getGroupId(),
                enrollment.getSubjectId(),
                enrollment.getSchoolYear(),
                enrollment.isActive(),
                enrollment.getCreatedAt()
        );
    }
}
