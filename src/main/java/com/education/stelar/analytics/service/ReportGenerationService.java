package com.education.stelar.analytics.service;

import com.education.stelar.analytics.dto.request.GenerateReportRequest;
import com.education.stelar.analytics.dto.response.ReportResponse;
import com.education.stelar.analytics.entity.Report;
import com.education.stelar.analytics.repository.ReportRepository;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;
import com.education.stelar.kernel.pagination.PagedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportGenerationService {

    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReportResponse generate(GenerateReportRequest request, UUID requestedBy) {
        UUID tenantId = TenantContext.getCurrentTenant();
        String title = request.title() != null ? request.title()
                : "Reporte " + request.reportType().name();

        Map<String, Object> params = new HashMap<>();
        if (request.groupId() != null) params.put("groupId", request.groupId().toString());
        if (request.subjectId() != null) params.put("subjectId", request.subjectId().toString());
        if (request.studentId() != null) params.put("studentId", request.studentId().toString());
        if (request.academicPeriodId() != null) params.put("academicPeriodId", request.academicPeriodId().toString());

        String parametersJson = serializeParameters(params);
        Report report = Report.create(request.reportType(), title, requestedBy, parametersJson);
        report = reportRepository.save(report);

        processAsync(report.getId(), tenantId);

        return ReportResponse.from(report);
    }

    private void processAsync(UUID reportId, UUID tenantId) {
        // Stub: en producción esto se procesaría en un @Async o job queue.
        // Por ahora marcamos como generating para simular el flujo.
        log.info("Procesando reporte {} para tenant {}", reportId, tenantId);
        reportRepository.findById(reportId).ifPresent(r -> {
            r.markGenerating();
            reportRepository.save(r);
        });
    }

    public PagedResponse<ReportResponse> listReports(Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Page<Report> page = reportRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        return PagedResponse.from(page, ReportResponse::from);
    }

    public ReportResponse getReport(UUID reportId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Report report = reportRepository.findByIdAndTenantId(reportId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));
        return ReportResponse.from(report);
    }

    public String getDownloadUrl(UUID reportId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Report report = reportRepository.findByIdAndTenantId(reportId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));
        if (report.getFileUrl() == null) {
            throw new com.education.stelar.kernel.exception.BusinessException(
                    "REPORT_NOT_READY", "El reporte aún no está disponible para descarga");
        }
        return report.getFileUrl();
    }

    private String serializeParameters(Map<String, Object> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
