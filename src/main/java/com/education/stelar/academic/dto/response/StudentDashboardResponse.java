package com.education.stelar.academic.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record StudentDashboardResponse(
        UUID studentId,
        String firstName,
        String lastName,
        String fullName,
        String studentCode,
        String email,
        List<EnrollmentSummary> enrollments
) {
    public record EnrollmentSummary(
            UUID enrollmentId,
            UUID groupId,
            String groupName,
            UUID subjectId,
            String subjectName,
            BigDecimal weightedAverage,
            AttendanceSummary attendance,
            List<GradeResponse> grades
    ) {
    }

    public record AttendanceSummary(
            int totalRecords,
            int present,
            int absent,
            int late,
            int justified,
            int attendancePct
    ) {
    }
}
