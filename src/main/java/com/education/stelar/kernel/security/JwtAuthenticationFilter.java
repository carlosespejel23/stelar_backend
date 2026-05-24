package com.education.stelar.kernel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.education.stelar.kernel.multitenancy.TenantContext;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Intercepta cada request, extrae y valida el JWT del header Authorization,
 * verifica que el token no esté en la blacklist de Redis,
 * y establece el SecurityContext y TenantContext para el hilo actual.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractToken(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                // Verificar que el token no fue revocado explícitamente (logout)
                String jti = jwtTokenProvider.getJtiFromToken(jwt);
                if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
                    log.debug("Token revocado (blacklist): {}", jti);
                    // No se establece autenticación — Spring Security rechazará el request
                    filterChain.doFilter(request, response);
                    return;
                }

                String email = jwtTokenProvider.getEmailFromToken(jwt);
                UUID tenantId = jwtTokenProvider.getTenantIdFromToken(jwt);
                List<String> roles = jwtTokenProvider.getRolesFromToken(jwt);

                TenantContext.setCurrentTenant(tenantId);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("No se pudo establecer la autenticación del usuario: {}", e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
