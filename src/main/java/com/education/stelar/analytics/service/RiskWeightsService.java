package com.education.stelar.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.analytics.dto.request.UpdateRiskWeightsRequest;
import com.education.stelar.analytics.dto.response.RiskWeightsResponse;
import com.education.stelar.analytics.entity.RiskWeights;
import com.education.stelar.analytics.repository.RiskWeightsRepository;
import com.education.stelar.kernel.exception.BusinessException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskWeightsService {

    private final RiskWeightsRepository riskWeightsRepository;

    public RiskWeightsResponse getWeights() {
        UUID tenantId = TenantContext.getCurrentTenant();
        RiskWeights weights = riskWeightsRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    RiskWeights def = RiskWeights.createDefault();
                    return def;
                });
        return RiskWeightsResponse.from(weights);
    }

    public RiskWeights getWeightsEntity() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return riskWeightsRepository.findByTenantId(tenantId)
                .orElseGet(RiskWeights::createDefault);
    }

    @Transactional
    public RiskWeightsResponse updateWeights(UpdateRiskWeightsRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        double sum = request.lowAverage() + request.negativeTrend() + request.lowAttendance()
                + request.consecutiveAbsences() + request.failedSubjects() + request.temporalUrgency();
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new BusinessException("INVALID_WEIGHTS", "Los pesos deben sumar 1.0 (actual: " + sum + ")");
        }
        RiskWeights weights = riskWeightsRepository.findByTenantId(tenantId)
                .orElseGet(RiskWeights::createDefault);
        weights.update(request.lowAverage(), request.negativeTrend(), request.lowAttendance(),
                request.consecutiveAbsences(), request.failedSubjects(), request.temporalUrgency());
        return RiskWeightsResponse.from(riskWeightsRepository.save(weights));
    }
}
