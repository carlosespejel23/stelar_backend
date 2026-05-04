package com.education.stelar.analytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.analytics.entity.RiskWeights;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskWeightsRepository extends JpaRepository<RiskWeights, UUID> {

    Optional<RiskWeights> findByTenantId(UUID tenantId);
}
