package com.education.stelar.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.Subject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {

    List<Subject> findAllByTenantId(UUID tenantId);

    List<Subject> findAllByTenantIdAndActive(UUID tenantId, boolean active);

    List<Subject> findAllByTenantIdAndTeacherId(UUID tenantId, UUID teacherId);

    Optional<Subject> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);
}
