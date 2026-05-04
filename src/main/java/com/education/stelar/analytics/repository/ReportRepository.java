package com.education.stelar.analytics.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.analytics.entity.Report;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    Page<Report> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    Optional<Report> findByIdAndTenantId(UUID id, UUID tenantId);
}
