package com.education.stelar.academic.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.Student;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    Page<Student> findAllByTenantId(UUID tenantId, Pageable pageable);

    Optional<Student> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByStudentCodeAndTenantId(String studentCode, UUID tenantId);

    Optional<Student> findByStudentCodeAndTenantId(String studentCode, UUID tenantId);
}
