package com.education.stelar.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.entity.Attendance;
import com.education.stelar.academic.entity.Enrollment;
import com.education.stelar.academic.entity.Grade;
import com.education.stelar.academic.entity.Subject;
import com.education.stelar.academic.repository.*;
import com.education.stelar.analytics.dto.response.DashboardStatsResponse;
import com.education.stelar.analytics.dto.response.GroupStatsResponse;
import com.education.stelar.analytics.entity.AlertStatus;
import com.education.stelar.analytics.entity.RiskLevel;
import com.education.stelar.analytics.repository.AcademicAlertRepository;
import com.education.stelar.analytics.repository.InAppNotificationRepository;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicStatsService {

    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final AcademicAlertRepository alertRepository;
    private final InAppNotificationRepository inAppNotificationRepository;

    public GroupStatsResponse getGroupStats(UUID groupId, UUID subjectId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        Subject subject = subjectRepository.findByIdAndTenantId(subjectId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada"));

        List<Enrollment> enrollments = enrollmentRepository
                .findAllByGroupIdAndSubjectIdAndActiveTrueAndTenantId(groupId, subjectId, tenantId);

        return computeGroupStats(groupId, subjectId, subject.getName(), enrollments, tenantId);
    }

    public List<GroupStatsResponse> getGroupStatsList(UUID groupId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        List<Enrollment> enrollments = enrollmentRepository.findAllByGroupIdAndTenantId(groupId, tenantId);
        Map<UUID, List<Enrollment>> bySubject = new HashMap<>();
        for (Enrollment e : enrollments) {
            bySubject.computeIfAbsent(e.getSubjectId(), k -> new ArrayList<>()).add(e);
        }

        List<GroupStatsResponse> result = new ArrayList<>();
        for (Map.Entry<UUID, List<Enrollment>> entry : bySubject.entrySet()) {
            UUID subjectId = entry.getKey();
            subjectRepository.findByIdAndTenantId(subjectId, tenantId).ifPresent(subject -> {
                result.add(computeGroupStats(groupId, subjectId, subject.getName(),
                        entry.getValue().stream().filter(Enrollment::isActive).toList(), tenantId));
            });
        }
        return result;
    }

    public DashboardStatsResponse getDashboard(UUID userId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        long totalStudents = studentRepository.findAllByTenantId(tenantId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long critical = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.CRITICAL, AlertStatus.ACTIVE);
        long high = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.HIGH, AlertStatus.ACTIVE);
        long medium = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.MEDIUM, AlertStatus.ACTIVE);
        long low = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.LOW, AlertStatus.ACTIVE);
        long unread = inAppNotificationRepository.countByUserIdAndTenantIdAndReadFalse(userId, tenantId);

        List<GroupStatsResponse> groupStats = new ArrayList<>();

        return new DashboardStatsResponse(totalStudents, critical + high + medium + low,
                critical, high, medium, low, unread, groupStats);
    }

    private GroupStatsResponse computeGroupStats(UUID groupId, UUID subjectId, String subjectName,
                                                  List<Enrollment> enrollments, UUID tenantId) {
        long totalStudents = enrollments.size();
        long studentsWithGrades = 0;
        BigDecimal gradeSum = BigDecimal.ZERO;
        long passingCount = 0;
        long failingCount = 0;
        long presentCount = 0;
        long absentCount = 0;
        long lateCount = 0;
        long justifiedCount = 0;

        BigDecimal passingGrade = new BigDecimal("6.0");

        for (Enrollment enrollment : enrollments) {
            List<Grade> grades = gradeRepository.findAllByEnrollmentIdAndTenantId(enrollment.getId(), tenantId);
            if (!grades.isEmpty()) {
                studentsWithGrades++;
                BigDecimal avg = grades.stream()
                        .map(Grade::getScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
                gradeSum = gradeSum.add(avg);
                if (avg.compareTo(passingGrade) >= 0) passingCount++; else failingCount++;
            }

            List<Attendance> attendances = attendanceRepository.findAllByEnrollmentId(enrollment.getId());
            for (Attendance a : attendances) {
                switch (a.getStatus()) {
                    case PRESENT -> presentCount++;
                    case ABSENT -> absentCount++;
                    case LATE -> lateCount++;
                    case JUSTIFIED -> justifiedCount++;
                }
            }
        }

        BigDecimal groupAverage = studentsWithGrades == 0 ? BigDecimal.ZERO :
                gradeSum.divide(BigDecimal.valueOf(studentsWithGrades), 2, RoundingMode.HALF_UP);

        long totalAttendanceRecords = presentCount + absentCount + lateCount + justifiedCount;
        double attendancePct = totalAttendanceRecords == 0 ? 100.0 :
                (double) (presentCount + lateCount) / totalAttendanceRecords * 100;

        Set<UUID> studentIds = new HashSet<>();
        enrollments.forEach(e -> studentIds.add(e.getStudentId()));

        long lowRisk = 0, mediumRisk = 0, highRisk = 0, criticalRisk = 0;
        /*for (UUID studentId : studentIds) {
            long crit = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.CRITICAL, AlertStatus.ACTIVE);
            // simplified: just count total per tenant for now; detailed per-student query would need additional repo method
        }*/

        return new GroupStatsResponse(groupId, subjectId, subjectName,
                totalStudents, studentsWithGrades, groupAverage,
                passingCount, failingCount,
                presentCount, absentCount,
                Math.round(attendancePct * 10.0) / 10.0,
                lowRisk, mediumRisk, highRisk, criticalRisk);
    }
}
