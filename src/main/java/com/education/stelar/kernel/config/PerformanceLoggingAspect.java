package com.education.stelar.kernel.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Aspecto de diagnóstico — SOLO activo en el perfil 'dev'.
 *
 * Loguea los métodos de los services de Identity que tarden más de 50ms.
 * Esto permite identificar cuellos de botella por capa sin modificar el código
 * de negocio.
 *
 * Para usar en producción (temporalmente): añadir "perf" al spring.profiles.active
 * y ajustar el umbral de 50ms al valor deseado.
 *
 * Ejemplo de output:
 *   WARN  PERF: AuthService.validate took 312 ms
 *   WARN  PERF: EmailVerificationService.sendVerificationEmail took 487 ms
 */
@Aspect
@Component
@Slf4j
@Profile("dev")
public class PerformanceLoggingAspect {

    private static final long THRESHOLD_MS = 50;

    @Around("execution(* com.academic.stellar.identity.service.*.*(..))")
    public Object logIdentityServiceTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        if (elapsed > THRESHOLD_MS) {
            log.warn("PERF: {}.{} took {} ms",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    elapsed);
        }
        return result;
    }
}
