package com.education.stelar.academic.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.academic.entity.Student;

public record StudentResponse(
        UUID id,
        UUID tenantId,
        String firstName,
        String lastName,
        String fullName,
        String studentCode,
        String email,
        boolean active,
        Instant createdAt
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getTenantId(),
                student.getFirstName(),
                student.getLastName(),
                student.getFullName(),
                student.getStudentCode(),
                student.getEmail(),
                student.isActive(),
                student.getCreatedAt()
        );
    }
}
