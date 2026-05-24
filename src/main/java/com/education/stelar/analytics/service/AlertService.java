package com.education.stelar.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.analytics.dto.request.DismissAlertRequest;
import com.education.stelar.analytics.dto.response.AcademicAlertResponse;
import com.education.stelar.analytics.dto.response.AlertSummaryResponse;
import com.education.stelar.analytics.entity.AcademicAlert;
import com.education.stelar.analytics.entity.AlertStatus;
import com.education.stelar.analytics.entity.RiskLevel;
import com.education.stelar.analytics.repository.AcademicAlertRepository;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;
import com.education.stelar.kernel.pagination.PagedResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertService {

    private final AcademicAlertRepository alertRepository;
    private final RiskDetectionService riskDetectionService;

    public PagedResponse<AcademicAlertResponse> getActiveAlerts(RiskLevel riskLevel, Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenant();
        Page<AcademicAlert> page;
        if (riskLevel != null) {
            page = alertRepository.findAllByTenantIdAndStatusAndRiskLevel(
                    tenantId, AlertStatus.ACTIVE, riskLevel, pageable);
        } else {
            page = alertRepository.findAllByTenantIdAndStatus(tenantId, AlertStatus.ACTIVE, pageable);
        }
        return PagedResponse.from(page, AcademicAlertResponse::from);
    }

    public List<AcademicAlertResponse> getAlertsByStudent(UUID studentId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        return alertRepository.findAllByStudentIdAndTenantId(studentId, tenantId)
                .stream().map(AcademicAlertResponse::from).toList();
    }

    public AlertSummaryResponse getSummary() {
        UUID tenantId = TenantContext.getCurrentTenant();
        long low = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.LOW, AlertStatus.ACTIVE);
        long medium = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.MEDIUM, AlertStatus.ACTIVE);
        long high = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.HIGH, AlertStatus.ACTIVE);
        long critical = alertRepository.countByTenantIdAndRiskLevelAndStatus(tenantId, RiskLevel.CRITICAL, AlertStatus.ACTIVE);
        return new AlertSummaryResponse(low, medium, high, critical, low + medium + high + critical);
    }

    @Transactional
    public AcademicAlertResponse dismiss(UUID alertId, UUID dismissedBy, DismissAlertRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        AcademicAlert alert = alertRepository.findByIdAndTenantId(alertId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));
        alert.dismiss(dismissedBy, request.reason());
        return AcademicAlertResponse.from(alertRepository.save(alert));
    }

    @Transactional
    public AcademicAlertResponse evaluateStudent(UUID studentId) {
        riskDetectionService.evaluateStudent(studentId);
        UUID tenantId = TenantContext.getCurrentTenant();
        return alertRepository.findAllByStudentIdAndTenantId(studentId, tenantId)
                .stream()
                .filter(a -> a.getStatus() == AlertStatus.ACTIVE)
                .findFirst()
                .map(AcademicAlertResponse::from)
                .orElse(null);
    }

    @Transactional
    public void evaluateAll() {
        riskDetectionService.evaluateAllStudents();
    }
}
