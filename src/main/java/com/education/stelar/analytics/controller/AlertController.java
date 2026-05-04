package com.education.stelar.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.analytics.dto.request.DismissAlertRequest;
import com.education.stelar.analytics.dto.response.AcademicAlertResponse;
import com.education.stelar.analytics.dto.response.AlertSummaryResponse;
import com.education.stelar.analytics.entity.RiskLevel;
import com.education.stelar.analytics.service.AlertService;
import com.education.stelar.kernel.pagination.PagedResponse;
import com.education.stelar.kernel.security.CurrentUser;
import com.education.stelar.kernel.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Gestión de alertas académicas")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Listar alertas activas paginadas, filtrables por nivel de riesgo")
    public ResponseEntity<PagedResponse<AcademicAlertResponse>> getActiveAlerts(
            @RequestParam(required = false) RiskLevel riskLevel,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(alertService.getActiveAlerts(riskLevel, pageable));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Obtener todas las alertas de un estudiante")
    public ResponseEntity<List<AcademicAlertResponse>> getByStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(alertService.getAlertsByStudent(studentId));
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen de alertas activas por nivel de riesgo")
    public ResponseEntity<AlertSummaryResponse> getSummary() {
        return ResponseEntity.ok(alertService.getSummary());
    }

    @PutMapping("/{id}/dismiss")
    @Operation(summary = "Descartar una alerta con motivo")
    public ResponseEntity<AcademicAlertResponse> dismiss(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody DismissAlertRequest request) {
        return ResponseEntity.ok(alertService.dismiss(id, principal.getId(), request));
    }

    @PostMapping("/evaluate/{studentId}")
    @Operation(summary = "Re-evaluar riesgo de un estudiante manualmente")
    public ResponseEntity<AcademicAlertResponse> evaluate(@PathVariable UUID studentId) {
        AcademicAlertResponse response = alertService.evaluateStudent(studentId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @PostMapping("/evaluate-all")
    @Operation(summary = "Disparar evaluación masiva de todos los estudiantes del tenant")
    public ResponseEntity<Void> evaluateAll() {
        alertService.evaluateAll();
        return ResponseEntity.accepted().build();
    }
}
