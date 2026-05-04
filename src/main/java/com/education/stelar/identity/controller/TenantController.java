package com.education.stelar.identity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.identity.dto.request.UpdateTenantRequest;
import com.education.stelar.identity.dto.response.TenantResponse;
import com.education.stelar.identity.service.TenantService;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant", description = "Información y configuración de la escuela")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/current")
    @Operation(summary = "Obtener información del tenant actual")
    public ResponseEntity<TenantResponse> getCurrent() {
        return ResponseEntity.ok(tenantService.findCurrent());
    }

    @PutMapping("/current")
    @Operation(summary = "Actualizar información del tenant actual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> update(@Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.update(request));
    }
}
