package com.education.stelar.analytics.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.analytics.entity.AcademicAlert;
import com.education.stelar.analytics.entity.AlertStatus;
import com.education.stelar.analytics.entity.RiskLevel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicAlertRepository extends JpaRepository<AcademicAlert, UUID> {

    Page<AcademicAlert> findAllByTenantIdAndStatus(UUID tenantId, AlertStatus status, Pageable pageable);

    Page<AcademicAlert> findAllByTenantIdAndStatusAndRiskLevel(
            UUID tenantId, AlertStatus status, RiskLevel riskLevel, Pageable pageable);

    List<AcademicAlert> findAllByStudentIdAndTenantId(UUID studentId, UUID tenantId);

    Optional<AcademicAlert> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<AcademicAlert> findByStudentIdAndAcademicPeriodIdAndStatusAndTenantId(
            UUID studentId, UUID academicPeriodId, AlertStatus status, UUID tenantId);

    long countByTenantIdAndRiskLevelAndStatus(UUID tenantId, RiskLevel riskLevel, AlertStatus status);

    long countByTenantIdAndStatus(UUID tenantId, AlertStatus status);

    List<AcademicAlert> findAllByTenantIdAndStatus(UUID tenantId, AlertStatus status);
}
