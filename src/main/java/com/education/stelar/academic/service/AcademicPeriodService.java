package com.education.stelar.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.dto.request.CreateAcademicPeriodRequest;
import com.education.stelar.academic.dto.request.UpdateAcademicPeriodRequest;
import com.education.stelar.academic.dto.response.AcademicPeriodResponse;
import com.education.stelar.academic.entity.AcademicPeriod;
import com.education.stelar.academic.entity.EvaluationPeriod;
import com.education.stelar.academic.repository.AcademicPeriodRepository;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademicPeriodService {

    private final AcademicPeriodRepository academicPeriodRepository;

    public List<AcademicPeriodResponse> findAll() {
        return academicPeriodRepository.findAllByTenantIdOrderByStartDateDesc(TenantContext.getCurrentTenant())
                .stream()
                .map(AcademicPeriodResponse::from)
                .toList();
    }

    public AcademicPeriodResponse findById(UUID id) {
        return academicPeriodRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .map(AcademicPeriodResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo académico", id));
    }

    public AcademicPeriodResponse findActive() {
        return academicPeriodRepository.findFirstByTenantIdAndActiveTrue(TenantContext.getCurrentTenant())
                .map(AcademicPeriodResponse::from)
                .orElseThrow(() -> new BusinessException("NO_ACTIVE_PERIOD",
                        "No hay un ciclo académico activo. Activa un ciclo primero."));
    }

    @Transactional
    public AcademicPeriodResponse create(CreateAcademicPeriodRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        if (request.endDate().isBefore(request.startDate()) || request.endDate().isEqual(request.startDate())) {
            throw new BusinessException("INVALID_DATES",
                    "La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        if (academicPeriodRepository.existsByNameAndTenantId(request.name(), tenantId)) {
            throw new BusinessException("PERIOD_NAME_EXISTS",
                    "Ya existe un ciclo académico con ese nombre.");
        }

        AcademicPeriod period = AcademicPeriod.create(
                request.name(), request.periodType(), request.startDate(), request.endDate());

        request.evaluationPeriods().forEach(epReq -> {
            EvaluationPeriod ep = EvaluationPeriod.create(
                    epReq.name(), epReq.weight(), epReq.sequenceOrder(),
                    epReq.startDate(), epReq.endDate());
            period.addEvaluationPeriod(ep);
        });

        if (!period.areWeightsValid()) {
            throw new BusinessException("INVALID_WEIGHTS",
                    "Los pesos de los períodos de evaluación deben sumar exactamente 100%.");
        }

        return AcademicPeriodResponse.from(academicPeriodRepository.save(period));
    }

    @Transactional
    public AcademicPeriodResponse update(UUID id, UpdateAcademicPeriodRequest request) {
        AcademicPeriod period = academicPeriodRepository.findByIdAndTenantId(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo académico", id));

        if (request.endDate().isBefore(request.startDate()) || request.endDate().isEqual(request.startDate())) {
            throw new BusinessException("INVALID_DATES",
                    "La fecha de fin debe ser posterior a la fecha de inicio.");
        }

        period.update(request.name(), request.startDate(), request.endDate());
        return AcademicPeriodResponse.from(academicPeriodRepository.save(period));
    }

    @Transactional
    public AcademicPeriodResponse activate(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Deactivate all other periods for this tenant
        academicPeriodRepository.findAllByTenantIdAndActiveTrue(tenantId)
                .forEach(p -> {
                    if (!p.getId().equals(id)) {
                        p.deactivate();
                        academicPeriodRepository.save(p);
                    }
                });

        AcademicPeriod period = academicPeriodRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo académico", id));

        period.activate();
        return AcademicPeriodResponse.from(academicPeriodRepository.save(period));
    }
}
