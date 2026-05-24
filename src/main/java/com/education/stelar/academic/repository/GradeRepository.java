package com.education.stelar.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.Grade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {

    List<Grade> findAllByEnrollmentStudentIdAndTenantId(UUID studentId, UUID tenantId);

    List<Grade> findAllByEnrollmentSubjectIdAndTenantId(UUID subjectId, UUID tenantId);

    List<Grade> findAllByEnrollmentStudentIdAndEnrollmentSubjectIdAndTenantId(
            UUID studentId, UUID subjectId, UUID tenantId);

    List<Grade> findAllByEnrollmentGroupIdAndEnrollmentSubjectIdAndTenantId(
            UUID groupId, UUID subjectId, UUID tenantId);

    List<Grade> findAllByEnrollmentIdAndTenantId(UUID enrollmentId, UUID tenantId);

    Optional<Grade> findByEnrollmentIdAndEvaluationPeriodId(UUID enrollmentId, UUID evaluationPeriodId);

    Optional<Grade> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query(value = """
            SELECT SUM(g.score * ep.weight / 100)
            FROM enrollments e
            JOIN grades g ON g.enrollment_id = e.id
            JOIN evaluation_periods ep ON ep.id = g.evaluation_period_id
            WHERE e.tenant_id = :tenantId
              AND e.student_id = :studentId
              AND e.subject_id = :subjectId
              AND ep.academic_period_id = :academicPeriodId
            """, nativeQuery = true)
    Optional<BigDecimal> findWeightedAverageByStudentAndSubjectAndPeriod(
            @Param("studentId") UUID studentId,
            @Param("subjectId") UUID subjectId,
            @Param("academicPeriodId") UUID academicPeriodId,
            @Param("tenantId") UUID tenantId);
}
