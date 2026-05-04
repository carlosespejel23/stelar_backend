package com.education.stelar.kernel.multitenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Extrae el tenant del header X-Tenant-ID para endpoints que no requieren JWT
 * (ej: /auth/register, donde el JWT aún no existe).
 *
 * Para requests autenticados, el JwtAuthenticationFilter ya establece
 * el TenantContext desde los claims del token — este filtro actúa como fallback.
 */
@Slf4j
@Component
public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Solo establece el tenant desde header si aún no fue establecido por el JWT filter
        if (TenantContext.getCurrentTenantOrNull() == null) {
            String tenantHeader = request.getHeader(TENANT_HEADER);
            if (StringUtils.hasText(tenantHeader)) {
                try {
                    TenantContext.setCurrentTenant(UUID.fromString(tenantHeader));
                } catch (IllegalArgumentException e) {
                    log.warn("Header {} contiene un UUID inválido: {}", TENANT_HEADER, tenantHeader);
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
