package com.education.stelar.analytics.service;

import java.math.BigDecimal;
import java.util.List;

public record RiskFactors(
        BigDecimal currentAverage,
        BigDecimal passingGrade,
        List<BigDecimal> orderedScores,
        int attendancePct,
        int minAttendancePct,
        int consecutiveAbsences,
        int failedSubjects,
        int currentWeek,
        int totalWeeks
) {
}
