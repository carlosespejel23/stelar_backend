package com.education.stelar.academic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.education.stelar.academic.entity.EvaluationPeriod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EvaluationPeriodRepository extends JpaRepository<EvaluationPeriod, UUID> {

    Optional<EvaluationPeriod> findByIdAndTenantId(UUID id, UUID tenantId);

    List<EvaluationPeriod> findAllByAcademicPeriodIdOrderBySequenceOrderAsc(UUID academicPeriodId);
}
