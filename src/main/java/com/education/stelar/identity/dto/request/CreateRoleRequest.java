package com.education.stelar.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateRoleRequest(

        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
        String name,

        @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
        String description,

        // IDs de los permisos a asignar al rol. Null o lista vacía = sin permisos.
        List<UUID> permissionIds
) {
}
