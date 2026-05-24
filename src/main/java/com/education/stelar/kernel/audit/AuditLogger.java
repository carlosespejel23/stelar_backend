package com.education.stelar.kernel.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Registra acciones relevantes para auditoría.
 * Actualmente loguea a nivel INFO; en producción puede integrarse
 * con CloudWatch o una tabla de auditoría.
 */
@Slf4j
@Component
public class AuditLogger {

    public void log(String action, UUID userId, UUID tenantId, String details) {
        log.info("[AUDIT] action={} userId={} tenantId={} details={}", action, userId, tenantId, details);
    }

    public void log(String action, UUID tenantId, String details) {
        log.info("[AUDIT] action={} tenantId={} details={}", action, tenantId, details);
    }
}
