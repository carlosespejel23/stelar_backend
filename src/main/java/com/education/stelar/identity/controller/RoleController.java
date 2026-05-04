package com.education.stelar.identity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.identity.dto.request.CreateRoleRequest;
import com.education.stelar.identity.dto.response.RoleResponse;
import com.education.stelar.identity.service.RoleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles del tenant")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Listar roles del tenant")
    public ResponseEntity<List<RoleResponse>> findAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    public ResponseEntity<RoleResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear rol personalizado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol personalizado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol personalizado",
               description = "Solo roles custom (no del sistema). Falla si hay usuarios asignados.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
