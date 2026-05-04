package com.education.stelar.kernel.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Blacklist de access tokens revocados, respaldada en Redis.
 *
 * Graceful degradation: si Redis no está disponible en runtime,
 * los métodos fallan silenciosamente — el token expirará de forma natural.
 * El sistema no se cae por un fallo temporal de Redis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:jwt:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Agrega el JTI a la blacklist con TTL = tiempo de vida restante del token.
     * Una vez que el token hubiera expirado naturalmente, Redis elimina la entrada.
     */
    public void blacklist(String jti, long remainingTtlMillis) {
        if (remainingTtlMillis <= 0) return;
        try {
            stringRedisTemplate.opsForValue()
                    .set(PREFIX + jti, "revoked", Duration.ofMillis(remainingTtlMillis));
        } catch (Exception e) {
            log.warn("Redis no disponible — token JTI '{}' no pudo registrarse en blacklist: {}",
                    jti, e.getMessage());
        }
    }

    /**
     * Retorna true si el JTI está en la blacklist (token explícitamente revocado).
     * En caso de error de Redis, retorna false: el token se considera válido
     * (graceful degradation — preferimos falso negativo sobre indisponibilidad).
     */
    public boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(PREFIX + jti));
        } catch (Exception e) {
            log.warn("Redis no disponible — no se pudo verificar blacklist para JTI '{}': {}",
                    jti, e.getMessage());
            return false;
        }
    }
}
