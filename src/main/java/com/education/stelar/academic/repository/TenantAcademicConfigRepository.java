package com.education.stelar.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.TenantAcademicConfig;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantAcademicConfigRepository extends JpaRepository<TenantAcademicConfig, UUID> {

    Optional<TenantAcademicConfig> findByTenantId(UUID tenantId);
}
