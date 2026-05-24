package com.education.stelar.academic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.academic.dto.request.TenantAcademicConfigRequest;
import com.education.stelar.academic.dto.response.TenantAcademicConfigResponse;
import com.education.stelar.academic.entity.TenantAcademicConfig;
import com.education.stelar.academic.repository.TenantAcademicConfigRepository;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantAcademicConfigService {

    private final TenantAcademicConfigRepository configRepository;

    public TenantAcademicConfigResponse getConfig() {
        UUID tenantId = TenantContext.getCurrentTenant();
        TenantAcademicConfig config = configRepository.findByTenantId(tenantId)
                .orElseGet(() -> {
                    TenantAcademicConfig defaultConfig = TenantAcademicConfig.createDefault();
                    return configRepository.save(defaultConfig);
                });
        return TenantAcademicConfigResponse.from(config);
    }

    @Transactional
    public TenantAcademicConfigResponse updateConfig(TenantAcademicConfigRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        TenantAcademicConfig config = configRepository.findByTenantId(tenantId)
                .orElseGet(TenantAcademicConfig::createDefault);

        config.update(
                request.periodType(),
                request.gradingScaleMin(),
                request.gradingScaleMax(),
                request.passingGrade(),
                request.attendanceMinimumPct()
        );

        return TenantAcademicConfigResponse.from(configRepository.save(config));
    }
}
