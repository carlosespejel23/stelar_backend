package com.education.stelar.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.analytics.dto.request.GenerateReportRequest;
import com.education.stelar.analytics.dto.response.ReportResponse;
import com.education.stelar.analytics.service.ReportGenerationService;
import com.education.stelar.kernel.pagination.PagedResponse;
import com.education.stelar.kernel.security.CurrentUser;
import com.education.stelar.kernel.security.UserPrincipal;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Generación y descarga de reportes académicos")
public class ReportController {

    private final ReportGenerationService reportGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "Solicitar generación de un reporte")
    public ResponseEntity<ReportResponse> generate(
            @Valid @RequestBody GenerateReportRequest request,
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(reportGenerationService.generate(request, principal.getId()));
    }

    @GetMapping
    @Operation(summary = "Listar reportes del tenant")
    public ResponseEntity<PagedResponse<ReportResponse>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reportGenerationService.listReports(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un reporte")
    public ResponseEntity<ReportResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reportGenerationService.getReport(id));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Redirigir a URL de descarga del reporte (S3 presigned URL)")
    public ResponseEntity<Void> download(@PathVariable UUID id) {
        String url = reportGenerationService.getDownloadUrl(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }
}
