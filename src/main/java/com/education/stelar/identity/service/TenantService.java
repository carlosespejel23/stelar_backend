package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.dto.request.UpdateTenantRequest;
import com.education.stelar.identity.dto.response.TenantResponse;
import com.education.stelar.identity.entity.Tenant;
import com.education.stelar.identity.repository.TenantRepository;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantResponse findCurrent() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return tenantRepository.findById(tenantId)
                .map(TenantResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
    }

    @Transactional
    public TenantResponse update(UpdateTenantRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        tenant.update(request.name(), request.description(), request.logoUrl());
        return TenantResponse.from(tenantRepository.save(tenant));
    }
}
