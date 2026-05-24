package com.education.stelar.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.dto.request.BatchGradeRequest;
import com.education.stelar.academic.dto.request.RecordGradeRequest;
import com.education.stelar.academic.dto.response.GradeResponse;
import com.education.stelar.academic.entity.Enrollment;
import com.education.stelar.academic.entity.EvaluationPeriod;
import com.education.stelar.academic.entity.Grade;
import com.education.stelar.academic.event.GradeRecordedEvent;
import com.education.stelar.academic.repository.AcademicPeriodRepository;
import com.education.stelar.academic.repository.EnrollmentRepository;
import com.education.stelar.academic.repository.EvaluationPeriodRepository;
import com.education.stelar.academic.repository.GradeRepository;
import com.education.stelar.kernel.event.EventPublisher;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final EventPublisher eventPublisher;

    public List<GradeResponse> findByStudent(UUID studentId) {
        return gradeRepository.findAllByEnrollmentStudentIdAndTenantId(studentId, TenantContext.getCurrentTenant())
                .stream().map(GradeResponse::from).toList();
    }

    public List<GradeResponse> findBySubject(UUID subjectId) {
        return gradeRepository.findAllByEnrollmentSubjectIdAndTenantId(subjectId, TenantContext.getCurrentTenant())
                .stream().map(GradeResponse::from).toList();
    }

    public List<GradeResponse> findByStudentAndSubject(UUID studentId, UUID subjectId) {
        return gradeRepository.findAllByEnrollmentStudentIdAndEnrollmentSubjectIdAndTenantId(
                        studentId, subjectId, TenantContext.getCurrentTenant())
                .stream().map(GradeResponse::from).toList();
    }

    public List<GradeResponse> findByGroupAndSubject(UUID groupId, UUID subjectId) {
        return gradeRepository.findAllByEnrollmentGroupIdAndEnrollmentSubjectIdAndTenantId(
                        groupId, subjectId, TenantContext.getCurrentTenant())
                .stream().map(GradeResponse::from).toList();
    }

    public BigDecimal getWeightedAverage(UUID studentId, UUID subjectId, UUID academicPeriodId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        UUID resolvedPeriodId = academicPeriodId != null
                ? academicPeriodId
                : academicPeriodRepository.findFirstByTenantIdAndActiveTrue(tenantId)
                .map(p -> p.getId())
                .orElseThrow(() -> new BusinessException("NO_ACTIVE_PERIOD",
                        "No hay un ciclo académico activo. Especifica un academicPeriodId."));

        return gradeRepository.findWeightedAverageByStudentAndSubjectAndPeriod(
                        studentId, subjectId, resolvedPeriodId, tenantId)
                .map(avg -> avg.setScale(2, RoundingMode.HALF_UP))
                .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public GradeResponse record(RecordGradeRequest request, UUID recordedBy) {
        UUID tenantId = TenantContext.getCurrentTenant();

        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(request.enrollmentId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción", request.enrollmentId()));

        if (!enrollment.isActive()) {
            throw new BusinessException("ENROLLMENT_INACTIVE",
                    "La inscripción no está activa.");
        }

        EvaluationPeriod evaluationPeriod = evaluationPeriodRepository
                .findByIdAndTenantId(request.evaluationPeriodId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Período de evaluación", request.evaluationPeriodId()));

        Grade grade = gradeRepository
                .findByEnrollmentIdAndEvaluationPeriodId(request.enrollmentId(), request.evaluationPeriodId())
                .map(existing -> {
                    existing.updateScore(request.score(), request.remarks());
                    return existing;
                })
                .orElseGet(() -> Grade.record(enrollment, evaluationPeriod, request.score(), recordedBy));

        grade = gradeRepository.save(grade);

        eventPublisher.publish(new GradeRecordedEvent(
                grade.getId(),
                enrollment.getStudentId(),
                enrollment.getSubjectId(),
                enrollment.getId(),
                evaluationPeriod.getId(),
                grade.getScore(),
                tenantId,
                Instant.now()));

        return GradeResponse.from(grade);
    }

    @Transactional
    public List<GradeResponse> recordBatch(BatchGradeRequest request, UUID recordedBy) {
        UUID tenantId = TenantContext.getCurrentTenant();

        EvaluationPeriod evaluationPeriod = evaluationPeriodRepository
                .findByIdAndTenantId(request.evaluationPeriodId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Período de evaluación", request.evaluationPeriodId()));

        return request.grades().stream().map(entry -> {
            Enrollment enrollment = enrollmentRepository
                    .findByStudentIdAndGroupIdAndSubjectIdAndTenantId(
                            entry.studentId(), request.groupId(), request.subjectId(), tenantId)
                    .orElseThrow(() -> new BusinessException("ENROLLMENT_NOT_FOUND",
                            "Inscripción no encontrada para el estudiante: " + entry.studentId()));

            Grade grade = gradeRepository
                    .findByEnrollmentIdAndEvaluationPeriodId(enrollment.getId(), evaluationPeriod.getId())
                    .map(existing -> {
                        existing.updateScore(entry.score(), entry.remarks());
                        return existing;
                    })
                    .orElseGet(() -> Grade.record(enrollment, evaluationPeriod, entry.score(), recordedBy));

            grade = gradeRepository.save(grade);

            eventPublisher.publish(new GradeRecordedEvent(
                    grade.getId(), enrollment.getStudentId(), enrollment.getSubjectId(),
                    enrollment.getId(), evaluationPeriod.getId(), grade.getScore(),
                    tenantId, Instant.now()));

            return GradeResponse.from(grade);
        }).collect(Collectors.toList());
    }
}
