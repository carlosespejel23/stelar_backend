package com.education.stelar.kernel.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-anotación para inyectar el usuario autenticado en parámetros de controller.
 *
 * Uso:
 *   public ResponseEntity<?> myEndpoint(@CurrentUser UserDetailsImpl user) { ... }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal
public @interface CurrentUser {
}
