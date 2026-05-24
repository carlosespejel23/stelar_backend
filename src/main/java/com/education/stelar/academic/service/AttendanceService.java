package com.education.stelar.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.dto.request.BatchAttendanceRequest;
import com.education.stelar.academic.dto.request.RecordAttendanceRequest;
import com.education.stelar.academic.dto.request.UpdateAttendanceRequest;
import com.education.stelar.academic.dto.response.AttendanceResponse;
import com.education.stelar.academic.entity.Attendance;
import com.education.stelar.academic.entity.Enrollment;
import com.education.stelar.academic.event.AttendanceRecordedEvent;
import com.education.stelar.academic.repository.AttendanceRepository;
import com.education.stelar.academic.repository.EnrollmentRepository;
import com.education.stelar.kernel.event.EventPublisher;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EventPublisher eventPublisher;

    public List<AttendanceResponse> findByStudent(UUID studentId) {
        return attendanceRepository.findAllByEnrollmentStudentIdAndTenantId(studentId, TenantContext.getCurrentTenant())
                .stream().map(AttendanceResponse::from).toList();
    }

    public List<AttendanceResponse> findBySubject(UUID subjectId) {
        return attendanceRepository.findAllByEnrollmentSubjectIdAndTenantId(subjectId, TenantContext.getCurrentTenant())
                .stream().map(AttendanceResponse::from).toList();
    }

    public List<AttendanceResponse> findByGroup(UUID groupId) {
        return attendanceRepository.findAllByEnrollmentGroupIdAndTenantId(groupId, TenantContext.getCurrentTenant())
                .stream().map(AttendanceResponse::from).toList();
    }

    public List<AttendanceResponse> findByGroupAndDate(UUID groupId, LocalDate date) {
        return attendanceRepository.findAllByEnrollmentGroupIdAndDateAndTenantId(
                        groupId, date, TenantContext.getCurrentTenant())
                .stream().map(AttendanceResponse::from).toList();
    }

    @Transactional
    public AttendanceResponse record(RecordAttendanceRequest request, UUID recordedBy) {
        UUID tenantId = TenantContext.getCurrentTenant();

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(request.enrollmentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción", request.enrollmentId()));

        if (!enrollment.isActive()) {
            throw new BusinessException("ENROLLMENT_INACTIVE", "La inscripción no está activa.");
        }

        Attendance attendance = attendanceRepository
                .findByEnrollmentIdAndDate(request.enrollmentId(), request.date())
                .map(existing -> {
                    existing.updateStatus(request.status(), request.justificationReason());
                    return existing;
                })
                .orElseGet(() -> Attendance.record(enrollment, request.date(), request.status(), recordedBy));

        attendance = attendanceRepository.save(attendance);

        eventPublisher.publish(new AttendanceRecordedEvent(
                attendance.getId(),
                enrollment.getStudentId(),
                enrollment.getId(),
                attendance.getStatus(),
                attendance.getDate(),
                tenantId,
                Instant.now()));

        return AttendanceResponse.from(attendance);
    }

    @Transactional
    public AttendanceResponse update(UUID id, UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia", id));

        attendance.updateStatus(request.status(), request.justificationReason());
        return AttendanceResponse.from(attendanceRepository.save(attendance));
    }

    @Transactional
    public List<AttendanceResponse> recordBatch(BatchAttendanceRequest request, UUID recordedBy) {
        UUID tenantId = TenantContext.getCurrentTenant();

        return request.records().stream().map(entry -> {
            Enrollment enrollment = enrollmentRepository
                    .findByStudentIdAndGroupIdAndSubjectIdAndTenantId(
                            entry.studentId(), request.groupId(), request.subjectId(), tenantId)
                    .orElseThrow(() -> new BusinessException("ENROLLMENT_NOT_FOUND",
                            "Inscripción no encontrada para el estudiante: " + entry.studentId()));

            Attendance attendance = attendanceRepository
                    .findByEnrollmentIdAndDate(enrollment.getId(), request.date())
                    .map(existing -> {
                        existing.updateStatus(entry.status(), entry.justificationReason());
                        return existing;
                    })
                    .orElseGet(() -> Attendance.record(enrollment, request.date(), entry.status(), recordedBy));

            attendance = attendanceRepository.save(attendance);

            eventPublisher.publish(new AttendanceRecordedEvent(
                    attendance.getId(), enrollment.getStudentId(), enrollment.getId(),
                    attendance.getStatus(), attendance.getDate(), tenantId, Instant.now()));

            return AttendanceResponse.from(attendance);
        }).collect(Collectors.toList());
    }
}
