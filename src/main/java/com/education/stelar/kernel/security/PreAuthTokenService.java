package com.education.stelar.kernel.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Token de corta vida que bridge entre /auth/validate y /auth/login.
 *
 * Flujo normal (2 pasos):
 *   1. /validate verifica BCrypt → genera pre-auth token (TTL 30s en Redis)
 *   2. /login recibe el token → lo consume (single-use) → omite BCrypt
 *
 * Fallback: si Redis no está disponible o el token expiró, /login cae
 * al path lento (BCrypt completo). No hay pérdida de seguridad.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PreAuthTokenService {

    private static final String PREFIX = "preauth:";
    private static final Duration TTL = Duration.ofSeconds(30);

    /**
     * Lua script: GET + DEL atómico, compatible con Redis 2.6+.
     * Reemplaza GETDEL (Redis 6.2+) para compatibilidad con Redis 5.x.
     */
    private static final DefaultRedisScript<String> GET_AND_DELETE_SCRIPT = new DefaultRedisScript<>(
            "local v = redis.call('GET', KEYS[1])\n" +
            "if v then redis.call('DEL', KEYS[1]) end\n" +
            "return v",
            String.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Crea un token de pre-autenticación asociado al email del usuario.
     * Retorna el token generado, o null si Redis no está disponible.
     */
    public String create(String email) {
        String token = UUID.randomUUID().toString();
        try {
            stringRedisTemplate.opsForValue().set(PREFIX + token, email.toLowerCase(), TTL);
            return token;
        } catch (Exception e) {
            log.warn("Redis no disponible — pre-auth token no almacenado: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Consume el token (lo elimina de Redis) y retorna el email asociado.
     * Si el token no existe, expiró, o Redis no está disponible, retorna null.
     * Es single-use: la primera llamada lo consume.
     */
    public String consumeAndGetEmail(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            return stringRedisTemplate.execute(GET_AND_DELETE_SCRIPT, List.of(PREFIX + token));
        } catch (Exception e) {
            log.warn("Redis no disponible — no se pudo validar pre-auth token: {}", e.getMessage());
            return null;
        }
    }
}
