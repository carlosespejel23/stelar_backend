package com.education.stelar.kernel.multitenancy;

import java.util.UUID;

/**
 * Almacena el tenant actual en un ThreadLocal por request.
 * Debe limpiarse al finalizar cada request para evitar memory leaks.
 */
public class TenantContext {

    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public static UUID getCurrentTenant() {
        UUID tenant = currentTenant.get();
        if (tenant == null) {
            throw new IllegalStateException("No hay tenant establecido en el contexto del hilo actual");
        }
        return tenant;
    }

    public static UUID getCurrentTenantOrNull() {
        return currentTenant.get();
    }

    public static void setCurrentTenant(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    public static void clear() {
        currentTenant.remove();
    }

    private TenantContext() {
    }
}
