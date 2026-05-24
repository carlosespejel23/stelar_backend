package com.education.stelar.academic.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.education.stelar.academic.entity.Grade;

public record GradeResponse(
        UUID id,
        UUID tenantId,
        UUID enrollmentId,
        UUID studentId,
        UUID subjectId,
        UUID evaluationPeriodId,
        String evaluationPeriodName,
        BigDecimal score,
        String remarks,
        UUID recordedBy,
        Instant createdAt
) {
    public static GradeResponse from(Grade grade) {
        return new GradeResponse(
                grade.getId(),
                grade.getTenantId(),
                grade.getEnrollment().getId(),
                grade.getEnrollment().getStudentId(),
                grade.getEnrollment().getSubjectId(),
                grade.getEvaluationPeriod().getId(),
                grade.getEvaluationPeriod().getName(),
                grade.getScore(),
                grade.getRemarks(),
                grade.getRecordedBy(),
                grade.getCreatedAt()
        );
    }
}
