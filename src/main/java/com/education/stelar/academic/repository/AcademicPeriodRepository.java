package com.education.stelar.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.AcademicPeriod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, UUID> {

    List<AcademicPeriod> findAllByTenantIdOrderByStartDateDesc(UUID tenantId);

    Optional<AcademicPeriod> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<AcademicPeriod> findFirstByTenantIdAndActiveTrue(UUID tenantId);

    List<AcademicPeriod> findAllByTenantIdAndActiveTrue(UUID tenantId);

    boolean existsByNameAndTenantId(String name, UUID tenantId);
}
