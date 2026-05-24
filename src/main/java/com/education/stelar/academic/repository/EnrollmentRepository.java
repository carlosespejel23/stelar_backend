package com.education.stelar.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.Enrollment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findAllByStudentIdAndTenantId(UUID studentId, UUID tenantId);

    List<Enrollment> findAllByGroupIdAndTenantId(UUID groupId, UUID tenantId);

    List<Enrollment> findAllBySubjectIdAndTenantId(UUID subjectId, UUID tenantId);

    Optional<Enrollment> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByStudentIdAndGroupIdAndSubjectIdAndTenantId(
            UUID studentId, UUID groupId, UUID subjectId, UUID tenantId);

    Optional<Enrollment> findByStudentIdAndGroupIdAndSubjectIdAndTenantId(
            UUID studentId, UUID groupId, UUID subjectId, UUID tenantId);

    List<Enrollment> findAllByGroupIdAndSubjectIdAndActiveTrueAndTenantId(
            UUID groupId, UUID subjectId, UUID tenantId);

    List<Enrollment> findAllByTenantIdAndActiveTrue(UUID tenantId);
}
