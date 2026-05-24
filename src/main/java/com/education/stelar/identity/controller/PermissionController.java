package com.education.stelar.identity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.identity.dto.request.CreatePermissionRequest;
import com.education.stelar.identity.dto.request.UpdatePermissionRequest;
import com.education.stelar.identity.dto.response.PermissionResponse;
import com.education.stelar.identity.service.PermissionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permisos", description = "Catálogo global de permisos del sistema")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "Listar todos los permisos activos")
    public ResponseEntity<List<PermissionResponse>> findAll() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo permiso en el catálogo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar permiso existente")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }
}
