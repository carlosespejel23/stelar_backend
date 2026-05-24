package com.education.stelar.kernel.security;

import java.util.UUID;

/**
 * Interface marcadora para el principal de seguridad.
 * Permite que el kernel obtenga el ID del usuario autenticado
 * sin depender directamente de la clase UserDetailsImpl del módulo identity.
 */
public interface UserPrincipal {

    UUID getId();

    UUID getTenantId();

    String getEmail();
}
