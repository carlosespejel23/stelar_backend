package com.education.stelar.academic.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.education.stelar.academic.entity.Attendance;
import com.education.stelar.academic.entity.AttendanceStatus;

public record AttendanceResponse(
        UUID id,
        UUID tenantId,
        UUID enrollmentId,
        UUID studentId,
        UUID subjectId,
        LocalDate date,
        AttendanceStatus status,
        String justificationReason,
        UUID recordedBy,
        Instant createdAt
) {
    public static AttendanceResponse from(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getTenantId(),
                attendance.getEnrollment().getId(),
                attendance.getEnrollment().getStudentId(),
                attendance.getEnrollment().getSubjectId(),
                attendance.getDate(),
                attendance.getStatus(),
                attendance.getJustificationReason(),
                attendance.getRecordedBy(),
                attendance.getCreatedAt()
        );
    }
}
