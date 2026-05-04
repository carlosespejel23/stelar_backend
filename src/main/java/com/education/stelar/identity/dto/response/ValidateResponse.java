package com.education.stelar.identity.dto.response;

import java.util.List;

public record ValidateResponse(
        String firstName,
        String lastName,
        String email,
        List<TenantSummaryResponse> tenants,

        /**
         * Token de corta vida (TTL 30 s, single-use) almacenado en Redis.
         * El frontend lo envía en /auth/login para omitir la re-verificación
         * de contraseña con BCrypt. Si Redis no está disponible será null —
         * /login acepta el path lento con password completo como fallback.
         */
        String preAuthToken
) {
}
