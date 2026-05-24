package com.education.stelar.academic.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.education.stelar.academic.entity.PeriodType;
import com.education.stelar.academic.entity.TenantAcademicConfig;
import com.education.stelar.academic.repository.TenantAcademicConfigRepository;
import com.education.stelar.identity.event.TenantCreatedEvent;

/**
 * Initializes the academic configuration when a new tenant is created.
 * Runs BEFORE_COMMIT to reuse the same transaction and TenantContext
 * already set by AuthService.register().
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantSetupListener {

    private final TenantAcademicConfigRepository configRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTenantCreated(TenantCreatedEvent event) {
        log.debug("Inicializando configuración académica para tenant: {}", event.tenantId());

        PeriodType periodType = parsePeriodType(event.periodType());
        TenantAcademicConfig config = TenantAcademicConfig.createWithPeriodType(periodType);
        configRepository.save(config);

        log.info("Configuración académica inicializada para tenant '{}' con período: {}",
                event.tenantSlug(), periodType);
    }

    private PeriodType parsePeriodType(String value) {
        if (value == null || value.isBlank()) {
            return PeriodType.SEMESTER;
        }
        try {
            return PeriodType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Tipo de período desconocido '{}', usando SEMESTER por defecto", value);
            return PeriodType.SEMESTER;
        }
    }
}
