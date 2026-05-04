package com.education.stelar.analytics.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.education.stelar.academic.entity.AttendanceStatus;
import com.education.stelar.academic.event.AttendanceRecordedEvent;
import com.education.stelar.academic.event.GradeRecordedEvent;
import com.education.stelar.analytics.service.RiskDetectionService;
import com.education.stelar.kernel.multitenancy.TenantContext;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiskEventListener {

    private final RiskDetectionService riskDetectionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGradeRecorded(GradeRecordedEvent event) {
        log.debug("Procesando GradeRecordedEvent para estudiante {}", event.studentId());
        TenantContext.setCurrentTenant(event.tenantId());
        try {
            riskDetectionService.evaluateStudent(event.studentId());
        } catch (Exception e) {
            log.error("Error al evaluar riesgo tras calificación: {}", e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAttendanceRecorded(AttendanceRecordedEvent event) {
        if (event.status() == AttendanceStatus.ABSENT) {
            log.debug("Procesando AttendanceRecordedEvent (ABSENT) para estudiante {}", event.studentId());
            TenantContext.setCurrentTenant(event.tenantId());
            try {
                riskDetectionService.evaluateStudent(event.studentId());
            } catch (Exception e) {
                log.error("Error al evaluar riesgo tras asistencia: {}", e.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
    }
}
