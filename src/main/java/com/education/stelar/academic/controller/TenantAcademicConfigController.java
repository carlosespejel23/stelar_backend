package com.education.stelar.academic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.academic.dto.request.TenantAcademicConfigRequest;
import com.education.stelar.academic.dto.response.TenantAcademicConfigResponse;
import com.education.stelar.academic.service.TenantAcademicConfigService;

@RestController
@RequestMapping("/api/v1/academic/config")
@RequiredArgsConstructor
@Tag(name = "Configuración académica", description = "Configuración del modelo académico del tenant")
public class TenantAcademicConfigController {

    private final TenantAcademicConfigService configService;

    @GetMapping
    @Operation(summary = "Obtener configuración académica del tenant")
    public ResponseEntity<TenantAcademicConfigResponse> getConfig() {
        return ResponseEntity.ok(configService.getConfig());
    }

    @PutMapping
    @Operation(summary = "Actualizar configuración académica del tenant")
    public ResponseEntity<TenantAcademicConfigResponse> updateConfig(
            @Valid @RequestBody TenantAcademicConfigRequest request) {
        return ResponseEntity.ok(configService.updateConfig(request));
    }
}
