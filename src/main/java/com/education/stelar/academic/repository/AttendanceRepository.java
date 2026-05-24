package com.education.stelar.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findAllByEnrollmentStudentIdAndTenantId(UUID studentId, UUID tenantId);

    List<Attendance> findAllByEnrollmentSubjectIdAndTenantId(UUID subjectId, UUID tenantId);

    List<Attendance> findAllByEnrollmentGroupIdAndTenantId(UUID groupId, UUID tenantId);

    List<Attendance> findAllByEnrollmentGroupIdAndDateAndTenantId(UUID groupId, LocalDate date, UUID tenantId);

    Optional<Attendance> findByEnrollmentIdAndDate(UUID enrollmentId, LocalDate date);

    Optional<Attendance> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Attendance> findAllByEnrollmentId(UUID enrollmentId);
}
