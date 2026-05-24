package com.education.stelar.analytics.service;

import org.springframework.stereotype.Component;

import com.education.stelar.analytics.entity.RiskLevel;
import com.education.stelar.analytics.entity.RiskWeights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
public class RiskCalculationEngine {

    public int calculateLowAverageScore(BigDecimal currentAvg, BigDecimal passingGrade) {
        if (currentAvg.compareTo(passingGrade) >= 0) return 0;
        double diff = passingGrade.subtract(currentAvg).doubleValue();
        return clamp((int) (diff * 25));
    }

    public int calculateNegativeTrendScore(List<BigDecimal> orderedScores) {
        if (orderedScores == null || orderedScores.size() < 2) return 0;
        BigDecimal latest = orderedScores.get(orderedScores.size() - 1);
        List<BigDecimal> previous = orderedScores.subList(0, orderedScores.size() - 1);
        BigDecimal avgPrevious = previous.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(previous.size()), 2, RoundingMode.HALF_UP);
        if (latest.compareTo(avgPrevious) >= 0) return 0;
        double drop = avgPrevious.subtract(latest).doubleValue();
        return clamp((int) (drop * 33));
    }

    public int calculateLowAttendanceScore(int attendancePct, int minAttendancePct) {
        if (attendancePct >= minAttendancePct) return 0;
        return clamp((int) ((minAttendancePct - attendancePct) * 2.5));
    }

    public int calculateConsecutiveAbsencesScore(int consecutiveAbsences) {
        return clamp(consecutiveAbsences * 20);
    }

    public int calculateFailedSubjectsScore(int failedSubjects) {
        return clamp(failedSubjects * 25);
    }

    public int calculateTemporalUrgencyScore(int currentWeek, int totalWeeks) {
        if (totalWeeks == 0) return 0;
        return clamp((int) (((double) currentWeek / totalWeeks) * 50));
    }

    public RiskResult calculate(RiskFactors factors, RiskWeights weights) {
        int f1 = calculateLowAverageScore(factors.currentAverage(), factors.passingGrade());
        int f2 = calculateNegativeTrendScore(factors.orderedScores());
        int f3 = calculateLowAttendanceScore(factors.attendancePct(), factors.minAttendancePct());
        int f4 = calculateConsecutiveAbsencesScore(factors.consecutiveAbsences());
        int f5 = calculateFailedSubjectsScore(factors.failedSubjects());
        int f6 = calculateTemporalUrgencyScore(factors.currentWeek(), factors.totalWeeks());

        double weighted = f1 * weights.getLowAverage()
                + f2 * weights.getNegativeTrend()
                + f3 * weights.getLowAttendance()
                + f4 * weights.getConsecutiveAbsences()
                + f5 * weights.getFailedSubjects()
                + f6 * weights.getTemporalUrgency();

        int totalScore = clamp((int) Math.round(weighted));
        RiskLevel level = RiskLevel.fromScore(totalScore);

        return new RiskResult(totalScore, level,
                Map.of(
                        "lowAverage", f1,
                        "negativeTrend", f2,
                        "lowAttendance", f3,
                        "consecutiveAbsences", f4,
                        "failedSubjects", f5,
                        "temporalUrgency", f6
                ));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
