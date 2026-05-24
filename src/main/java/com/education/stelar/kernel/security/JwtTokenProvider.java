package com.education.stelar.kernel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.education.stelar.kernel.config.AppProperties;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Genera y valida JSON Web Tokens con jjwt 0.12+.
 *
 * Claims del access token:
 *   - sub      → email del usuario
 *   - jti      → ID único del token (para blacklist al logout)
 *   - userId   → UUID del usuario
 *   - tenantId → UUID del tenant
 *   - roles    → lista de nombres de roles (ej: ["TEACHER", "ADMIN"])
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AppProperties appProperties;

    public String generateAccessToken(String email, UUID userId, UUID tenantId, List<String> roles) {
        long expiration = appProperties.getJwt().getAccessTokenExpiration();
        return Jwts.builder()
                .subject(email)
                .id(UUID.randomUUID().toString())          // jti — único por token, usado en blacklist
                .claim("userId", userId.toString())
                .claim("tenantId", tenantId.toString())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(parseClaims(token).get("userId", String.class));
    }

    public UUID getTenantIdFromToken(String token) {
        return UUID.fromString(parseClaims(token).get("tenantId", String.class));
    }

    public List<String> getRolesFromToken(String token) {
        Object roles = parseClaims(token).get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    /** Retorna el JTI (JWT ID) del token, usado para la blacklist al logout. */
    public String getJtiFromToken(String token) {
        return parseClaims(token).getId();
    }

    /** Retorna la fecha de expiración del token para calcular el TTL de la blacklist. */
    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expirado: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT inválido: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT vacío o nulo: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(appProperties.getJwt().getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
