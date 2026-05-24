package com.education.stelar.analytics.service;

import com.education.stelar.academic.entity.AcademicPeriod;
import com.education.stelar.academic.entity.Attendance;
import com.education.stelar.academic.entity.AttendanceStatus;
import com.education.stelar.academic.entity.Enrollment;
import com.education.stelar.academic.entity.Grade;
import com.education.stelar.academic.repository.AcademicPeriodRepository;
import com.education.stelar.academic.repository.AttendanceRepository;
import com.education.stelar.academic.repository.EnrollmentRepository;
import com.education.stelar.academic.repository.GradeRepository;
import com.education.stelar.academic.repository.TenantAcademicConfigRepository;
import com.education.stelar.analytics.entity.AcademicAlert;
import com.education.stelar.analytics.entity.AlertStatus;
import com.education.stelar.analytics.entity.RiskWeights;
import com.education.stelar.analytics.repository.AcademicAlertRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RiskDetectionService {

    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TenantAcademicConfigRepository tenantAcademicConfigRepository;
    private final AcademicAlertRepository alertRepository;
    private final RiskWeightsService riskWeightsService;
    private final RiskCalculationEngine riskCalculationEngine;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void evaluateStudent(UUID studentId) {
        UUID tenantId = com.education.stelar.kernel.multitenancy.TenantContext.getCurrentTenant();
        Optional<AcademicPeriod> activePeriodOpt = academicPeriodRepository.findFirstByTenantIdAndActiveTrue(tenantId);
        if (activePeriodOpt.isEmpty()) {
            log.debug("No hay período activo para tenant {}, saltando evaluación", tenantId);
            return;
        }
        AcademicPeriod activePeriod = activePeriodOpt.get();

        var config = tenantAcademicConfigRepository.findByTenantId(tenantId).orElse(null);
        BigDecimal passingGrade = config != null ? config.getPassingGrade() : new BigDecimal("6.0");
        int minAttendancePct = config != null ? config.getAttendanceMinimumPct() : 80;

        List<Enrollment> enrollments = enrollmentRepository.findAllByStudentIdAndTenantId(studentId, tenantId)
                .stream().filter(Enrollment::isActive).toList();

        if (enrollments.isEmpty()) return;

        RiskFactors factors = buildRiskFactors(studentId, tenantId, enrollments, activePeriod,
                passingGrade, minAttendancePct);
        RiskWeights weights = riskWeightsService.getWeightsEntity();
        RiskResult result = riskCalculationEngine.calculate(factors, weights);

        String factorScoresJson = serializeFactorScores(result.factorScores());

        Optional<AcademicAlert> existingAlert = alertRepository
                .findByStudentIdAndAcademicPeriodIdAndStatusAndTenantId(
                        studentId, activePeriod.getId(), AlertStatus.ACTIVE, tenantId);

        AcademicAlert alert;
        boolean isNew = false;
        if (existingAlert.isPresent()) {
            alert = existingAlert.get();
            alert.updateRisk(result.level(), result.totalScore(), factorScoresJson);
        } else {
            alert = AcademicAlert.create(studentId, activePeriod.getId(),
                    result.level(), result.totalScore(), factorScoresJson);
            isNew = true;
        }

        alertRepository.save(alert);

        if (isNew) {
            notificationService.notifyAlert(alert, tenantId);
        }
    }

    @Transactional
    public void evaluateAllStudents() {
        UUID tenantId = com.education.stelar.kernel.multitenancy.TenantContext.getCurrentTenant();
        Optional<AcademicPeriod> activePeriodOpt = academicPeriodRepository.findFirstByTenantIdAndActiveTrue(tenantId);
        if (activePeriodOpt.isEmpty()) {
            log.info("No hay período activo para tenant {}", tenantId);
            return;
        }

        Set<UUID> studentIds = enrollmentRepository.findAllByTenantIdAndActiveTrue(tenantId)
                .stream()
                .map(Enrollment::getStudentId)
                .collect(Collectors.toSet());

        log.info("Evaluando {} estudiantes para tenant {}", studentIds.size(), tenantId);
        for (UUID studentId : studentIds) {
            try {
                evaluateStudent(studentId);
            } catch (Exception e) {
                log.error("Error evaluando estudiante {}: {}", studentId, e.getMessage());
            }
        }
    }

    private RiskFactors buildRiskFactors(UUID studentId, UUID tenantId,
                                          List<Enrollment> enrollments,
                                          AcademicPeriod activePeriod,
                                          BigDecimal passingGrade,
                                          int minAttendancePct) {
        List<BigDecimal> allScoresByPeriod = new ArrayList<>();
        int totalAttendance = 0;
        int presentCount = 0;
        int maxConsecutiveAbsences = 0;
        int failedSubjects = 0;

        for (Enrollment enrollment : enrollments) {
            List<Grade> grades = gradeRepository.findAllByEnrollmentIdAndTenantId(enrollment.getId(), tenantId);
            grades.sort(Comparator.comparing(g -> g.getEvaluationPeriod().getSequenceOrder()));
            grades.forEach(g -> allScoresByPeriod.add(g.getScore()));

            if (!grades.isEmpty()) {
                BigDecimal avg = grades.stream()
                        .map(Grade::getScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
                if (avg.compareTo(passingGrade) < 0) failedSubjects++;
            }

            List<Attendance> attendances = attendanceRepository.findAllByEnrollmentId(enrollment.getId());
            attendances.sort(Comparator.comparing(Attendance::getDate));
            totalAttendance += attendances.size();
            presentCount += attendances.stream()
                    .filter(a -> a.getStatus() == AttendanceStatus.PRESENT || a.getStatus() == AttendanceStatus.LATE)
                    .count();
            maxConsecutiveAbsences = Math.max(maxConsecutiveAbsences,
                    countMaxConsecutiveAbsences(attendances));
        }

        BigDecimal overallAvg = allScoresByPeriod.isEmpty() ? passingGrade :
                allScoresByPeriod.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(allScoresByPeriod.size()), 2, RoundingMode.HALF_UP);

        int attendancePct = totalAttendance == 0 ? 100 :
                (int) Math.round((double) presentCount / totalAttendance * 100);

        return new RiskFactors(
                overallAvg,
                passingGrade,
                allScoresByPeriod,
                attendancePct,
                minAttendancePct,
                maxConsecutiveAbsences,
                failedSubjects,
                activePeriod.getCurrentWeek(),
                activePeriod.getTotalWeeks()
        );
    }

    private int countMaxConsecutiveAbsences(List<Attendance> sortedAttendances) {
        int max = 0;
        int current = 0;
        for (Attendance a : sortedAttendances) {
            if (a.countsAsAbsent()) {
                current++;
                max = Math.max(max, current);
            } else {
                current = 0;
            }
        }
        return max;
    }

    private String serializeFactorScores(Map<String, Integer> factorScores) {
        try {
            return objectMapper.writeValueAsString(factorScores);
        } catch (JsonProcessingException e) {
            log.warn("No se pudo serializar factorScores: {}", e.getMessage());
            return "{}";
        }
    }
}
