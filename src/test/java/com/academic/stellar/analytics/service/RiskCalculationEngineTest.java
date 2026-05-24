package com.academic.stellar.analytics.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.education.stelar.analytics.entity.RiskLevel;
import com.education.stelar.analytics.entity.RiskWeights;
import com.education.stelar.analytics.service.RiskCalculationEngine;
import com.education.stelar.analytics.service.RiskFactors;
import com.education.stelar.analytics.service.RiskResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RiskCalculationEngine")
class RiskCalculationEngineTest {

    private RiskCalculationEngine engine;
    private RiskWeights defaultWeights;

    @BeforeEach
    void setUp() {
        engine = new RiskCalculationEngine();
        defaultWeights = RiskWeights.createDefault();
    }

    // ---------------------------------------------------------------
    // F1: Low Average
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("F1 — Promedio bajo")
    class LowAverageScore {

        @Test
        @DisplayName("should_return0_when_averageIsAbovePassingGrade")
        void should_return0_when_averageIsAbovePassingGrade() {
            int score = engine.calculateLowAverageScore(
                    new BigDecimal("8.0"), new BigDecimal("6.0"));
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("should_return0_when_averageEqualsPassingGrade")
        void should_return0_when_averageEqualsPassingGrade() {
            int score = engine.calculateLowAverageScore(
                    new BigDecimal("6.0"), new BigDecimal("6.0"));
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("should_returnPositiveScore_when_averageIsBelowPassingGrade")
        void should_returnPositiveScore_when_averageIsBelowPassingGrade() {
            // diff = 6.0 - 4.0 = 2.0; score = 2.0 * 25 = 50
            int score = engine.calculateLowAverageScore(
                    new BigDecimal("4.0"), new BigDecimal("6.0"));
            assertThat(score).isEqualTo(50);
        }

        @Test
        @DisplayName("should_clampAt100_when_diffIsVeryLarge")
        void should_clampAt100_when_diffIsVeryLarge() {
            // diff = 10.0; 10*25 = 250, clamped to 100
            int score = engine.calculateLowAverageScore(
                    new BigDecimal("0.0"), new BigDecimal("10.0"));
            assertThat(score).isEqualTo(100);
        }

        @Test
        @DisplayName("should_returnSmallScore_when_justBelowPassingGrade")
        void should_returnSmallScore_when_justBelowPassingGrade() {
            // diff = 0.2; 0.2*25 = 5
            int score = engine.calculateLowAverageScore(
                    new BigDecimal("5.8"), new BigDecimal("6.0"));
            assertThat(score).isEqualTo(5);
        }
    }

    // ---------------------------------------------------------------
    // F2: Negative Trend
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("F2 — Tendencia negativa")
    class NegativeTrendScore {

        @Test
        @DisplayName("should_return0_when_fewerThan2Scores")
        void should_return0_when_fewerThan2Scores() {
            assertThat(engine.calculateNegativeTrendScore(List.of())).isEqualTo(0);
            assertThat(engine.calculateNegativeTrendScore(
                    List.of(new BigDecimal("7.0")))).isEqualTo(0);
        }

        @Test
        @DisplayName("should_return0_when_latestScoreIsHigherThanAvgPrevious")
        void should_return0_when_latestScoreIsHigherThanAvgPrevious() {
            // avgPrev = 5.0, latest = 8.0 → improving
            int score = engine.calculateNegativeTrendScore(
                    List.of(new BigDecimal("5.0"), new BigDecimal("8.0")));
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("should_returnPositiveScore_when_latestDropsBelowAvgPrevious")
        void should_returnPositiveScore_when_latestDropsBelowAvgPrevious() {
            // avgPrev = avg(8, 8) = 8.0, latest = 5.0 → drop = 3.0; 3*33 = 99
            int score = engine.calculateNegativeTrendScore(List.of(
                    new BigDecimal("8.0"), new BigDecimal("8.0"), new BigDecimal("5.0")));
            assertThat(score).isEqualTo(99);
        }

        @Test
        @DisplayName("should_return0_when_latestEqualsAvgPrevious")
        void should_return0_when_latestEqualsAvgPrevious() {
            int score = engine.calculateNegativeTrendScore(List.of(
                    new BigDecimal("7.0"), new BigDecimal("7.0")));
            assertThat(score).isEqualTo(0);
        }

        @Test
        @DisplayName("should_clampAt100_when_dropIsVeryLarge")
        void should_clampAt100_when_dropIsVeryLarge() {
            // avgPrev = 10, latest = 0 → drop = 10; 10*33 = 330, clamped to 100
            int score = engine.calculateNegativeTrendScore(List.of(
                    new BigDecimal("10.0"), new BigDecimal("0.0")));
            assertThat(score).isEqualTo(100);
        }
    }

    // ---------------------------------------------------------------
    // F3: Low Attendance
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("F3 — Baja asistencia")
    class LowAttendanceScore {

        @Test
        @DisplayName("should_return0_when_attendanceMeetsMinimum")
        void should_return0_when_attendanceMeetsMinimum() {
            assertThat(engine.calculateLowAttendanceScore(80, 80)).isEqualTo(0);
            assertThat(engine.calculateLowAttendanceScore(90, 80)).isEqualTo(0);
        }

        @Test
        @DisplayName("should_returnPositiveScore_when_attendanceIsBelowMinimum")
        void should_returnPositiveScore_when_attendanceIsBelowMinimum() {
            // min=80, actual=60 → diff=20; 20*2.5 = 50
            assertThat(engine.calculateLowAttendanceScore(60, 80)).isEqualTo(50);
        }

        @Test
        @DisplayName("should_clampAt100_when_attendanceIsVeryLow")
        void should_clampAt100_when_attendanceIsVeryLow() {
            // min=80, actual=0 → diff=80; 80*2.5=200, clamped to 100
            assertThat(engine.calculateLowAttendanceScore(0, 80)).isEqualTo(100);
        }
    }

    // ---------------------------------------------------------------
    // F4: Consecutive Absences
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("F4 — Faltas consecutivas")
    class ConsecutiveAbsencesScore {

        @Test
        @DisplayName("should_return0_when_noAbsences")
        void should_return0_when_noAbsences() {
            assertThat(engine.calculateConsecutiveAbsencesScore(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("should_return20_per_absence_up_to_100")
        void should_return20PerAbsence() {
            assertThat(engine.calculateConsecutiveAbsencesScore(1)).isEqualTo(20);
            assertThat(engine.calculateConsecutiveAbsencesScore(3)).isEqualTo(60);
            assertThat(engine.calculateConsecutiveAbsencesScore(5)).isEqualTo(100);
        }

        @Test
        @DisplayName("should_clampAt100_when_moreThan5ConsecutiveAbsences")
        void should_clampAt100_when_excessiveAbsences() {
            assertThat(engine.calculateConsecutiveAbsencesScore(10)).isEqualTo(100);
        }
    }

    // ---------------------------------------------------------------
    // F5: Failed Subjects
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("F5 — Materias reprobadas")
    class FailedSubjectsScore {

        @Test
        @DisplayName("should_return0_when_noFailedSubjects")
        void should_return0_when_noFailedSubjects() {
            assertThat(engine.calculateFailedSubjectsScore(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("should_return25_per_failedSubject")
        void should_return25PerFailedSubject() {
            assertThat(engine.calculateFailedSubjectsScore(1)).isEqualTo(25);
            assertThat(engine.calculateFailedSubjectsScore(2)).isEqualTo(50);
            assertThat(engine.calculateFailedSubjectsScore(4)).isEqualTo(100);
        }

        @Test
        @DisplayName("should_clampAt100_when_moreThan4FailedSubjects")
        void should_clampAt100_when_manySubjectsFailed() {
            assertThat(engine.calculateFailedSubjectsScore(10)).isEqualTo(100);
        }
    }

    // ---------------------------------------------------------------
    // F6: Temporal Urgency
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("F6 — Urgencia temporal")
    class TemporalUrgencyScore {

        @Test
        @DisplayName("should_return0_when_totalWeeksIsZero")
        void should_return0_when_totalWeeksIsZero() {
            assertThat(engine.calculateTemporalUrgencyScore(5, 0)).isEqualTo(0);
        }

        @Test
        @DisplayName("should_returnHalfMaxAt50pctProgress")
        void should_returnHalfMaxAt50pctProgress() {
            // 5/10 * 50 = 25
            assertThat(engine.calculateTemporalUrgencyScore(5, 10)).isEqualTo(25);
        }

        @Test
        @DisplayName("should_return50_when_periodIsComplete")
        void should_return50_when_periodIsComplete() {
            assertThat(engine.calculateTemporalUrgencyScore(10, 10)).isEqualTo(50);
        }

        @Test
        @DisplayName("should_return0_when_currentWeekIsZero")
        void should_return0_when_atBeginning() {
            assertThat(engine.calculateTemporalUrgencyScore(0, 16)).isEqualTo(0);
        }
    }

    // ---------------------------------------------------------------
    // Full calculation + Risk Level thresholds
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("Cálculo completo — niveles de riesgo")
    class FullCalculation {

        @Test
        @DisplayName("should_returnLow_when_studentIsDoingWell")
        void should_returnLow_when_studentIsDoingWell() {
            RiskFactors factors = new RiskFactors(
                    new BigDecimal("8.5"),      // currentAverage — above passing
                    new BigDecimal("6.0"),      // passingGrade
                    List.of(new BigDecimal("8.0"), new BigDecimal("8.5"), new BigDecimal("9.0")), // improving
                    90, 80,                     // attendance 90%, min 80%
                    0,                          // no consecutive absences
                    0,                          // no failed subjects
                    2, 16                       // early in semester
            );
            RiskResult result = engine.calculate(factors, defaultWeights);
            assertThat(result.totalScore()).isLessThan(25);
            assertThat(result.level()).isEqualTo(RiskLevel.LOW);
        }

        @Test
        @DisplayName("should_returnCritical_when_studentIsInSevereRisk")
        void should_returnCritical_when_studentIsInSevereRisk() {
            RiskFactors factors = new RiskFactors(
                    new BigDecimal("3.0"),      // very low average
                    new BigDecimal("6.0"),      // passingGrade
                    List.of(new BigDecimal("7.0"), new BigDecimal("5.0"), new BigDecimal("3.0")), // dropping
                    50, 80,                     // attendance 50%, well below min
                    5,                          // 5 consecutive absences
                    3,                          // 3 failed subjects
                    14, 16                      // near end of semester
            );
            RiskResult result = engine.calculate(factors, defaultWeights);
            assertThat(result.totalScore()).isGreaterThanOrEqualTo(75);
            assertThat(result.level()).isEqualTo(RiskLevel.CRITICAL);
        }

        @Test
        @DisplayName("should_includAllSixFactorsInResult")
        void should_includeAllSixFactorsInResult() {
            RiskFactors factors = new RiskFactors(
                    new BigDecimal("5.0"), new BigDecimal("6.0"),
                    List.of(new BigDecimal("6.0"), new BigDecimal("5.0")),
                    70, 80, 2, 1, 8, 16
            );
            RiskResult result = engine.calculate(factors, defaultWeights);
            assertThat(result.factorScores()).containsKeys(
                    "lowAverage", "negativeTrend", "lowAttendance",
                    "consecutiveAbsences", "failedSubjects", "temporalUrgency");
        }

        @Test
        @DisplayName("should_respectCustomWeights_when_weightsAreAdjusted")
        void should_respectCustomWeights_when_weightsAreAdjusted() {
            // Assign all weight to F1 (lowAverage), others = 0
            RiskWeights allOnAverage = RiskWeights.createDefault();
            allOnAverage.update(1.0, 0.0, 0.0, 0.0, 0.0, 0.0);

            RiskFactors factors = new RiskFactors(
                    new BigDecimal("3.0"), new BigDecimal("6.0"), // diff=3, f1=75
                    List.of(new BigDecimal("8.0"), new BigDecimal("3.0")), // f2 would be high
                    50, 80, // f3 would be high
                    5, 3, 14, 16
            );
            RiskResult result = engine.calculate(factors, allOnAverage);
            // With all weight on F1: 3.0 * 25 = 75 → CRITICAL
            assertThat(result.totalScore()).isEqualTo(75);
            assertThat(result.level()).isEqualTo(RiskLevel.CRITICAL);
        }

        @Test
        @DisplayName("should_returnMedium_when_scoreIsBetween25And49")
        void should_returnMedium_when_scoreIsBetween25And49() {
            assertThat(RiskLevel.fromScore(25)).isEqualTo(RiskLevel.MEDIUM);
            assertThat(RiskLevel.fromScore(49)).isEqualTo(RiskLevel.MEDIUM);
        }

        @Test
        @DisplayName("should_returnHigh_when_scoreIsBetween50And74")
        void should_returnHigh_when_scoreIsBetween50And74() {
            assertThat(RiskLevel.fromScore(50)).isEqualTo(RiskLevel.HIGH);
            assertThat(RiskLevel.fromScore(74)).isEqualTo(RiskLevel.HIGH);
        }

        @Test
        @DisplayName("should_scoreNeverExceed100OrGoBelowZero")
        void should_scoreNeverExceed100OrGoBelowZero() {
            // All factors at max
            RiskFactors maxFactors = new RiskFactors(
                    BigDecimal.ZERO, new BigDecimal("10.0"),
                    List.of(new BigDecimal("10.0"), BigDecimal.ZERO),
                    0, 100, 100, 100, 100, 10
            );
            RiskResult result = engine.calculate(maxFactors, defaultWeights);
            assertThat(result.totalScore()).isBetween(0, 100);

            // All factors at min
            RiskFactors minFactors = new RiskFactors(
                    new BigDecimal("10.0"), new BigDecimal("6.0"),
                    List.of(new BigDecimal("8.0"), new BigDecimal("9.0"), new BigDecimal("10.0")),
                    100, 80, 0, 0, 0, 16
            );
            RiskResult minResult = engine.calculate(minFactors, defaultWeights);
            assertThat(minResult.totalScore()).isBetween(0, 100);
        }
    }
}
