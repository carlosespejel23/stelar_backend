package com.education.stelar.analytics.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record GroupStatsResponse(
        UUID groupId,
        UUID subjectId,
        String subjectName,
        long totalStudents,
        long studentsWithGrades,
        BigDecimal groupAverage,
        long passingCount,
        long failingCount,
        long presentCount,
        long absentCount,
        double attendancePct,
        long lowRiskCount,
        long mediumRiskCount,
        long highRiskCount,
        long criticalRiskCount
) {
}
