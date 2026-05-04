package com.education.stelar.identity.dto.response;

import java.util.List;

/**
 * Respuesta del endpoint GET /auth/session.
 * Consolida los 3 endpoints separados (me, me/permissions, me/tenants)
 * en una sola llamada para reducir round-trips post-login.
 */
public record SessionResponse(
        UserResponse user,
        List<String> permissions,
        List<TenantSummaryResponse> tenants
) {
}
