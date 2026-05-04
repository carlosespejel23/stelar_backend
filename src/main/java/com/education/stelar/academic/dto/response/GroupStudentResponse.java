package com.education.stelar.academic.dto.response;

import java.util.UUID;

import com.education.stelar.academic.entity.Enrollment;
import com.education.stelar.academic.entity.Student;

public record GroupStudentResponse(
        UUID enrollmentId,
        UUID studentId,
        String studentCode,
        String firstName,
        String lastName,
        String fullName,
        String email,
        boolean enrollmentActive
) {
    public static GroupStudentResponse from(Enrollment enrollment, Student student) {
        return new GroupStudentResponse(
                enrollment.getId(),
                student.getId(),
                student.getStudentCode(),
                student.getFirstName(),
                student.getLastName(),
                student.getFullName(),
                student.getEmail(),
                enrollment.isActive()
        );
    }
}
