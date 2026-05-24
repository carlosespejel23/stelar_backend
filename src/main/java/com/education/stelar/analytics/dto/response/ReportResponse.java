package com.education.stelar.analytics.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.analytics.entity.Report;
import com.education.stelar.analytics.entity.ReportStatus;
import com.education.stelar.analytics.entity.ReportType;

public record ReportResponse(
        UUID id,
        UUID tenantId,
        ReportType reportType,
        String title,
        ReportStatus status,
        String fileUrl,
        Instant generatedAt,
        UUID requestedBy,
        String errorMessage,
        Instant createdAt
) {
    public static ReportResponse from(Report r) {
        return new ReportResponse(
                r.getId(), r.getTenantId(),
                r.getReportType(), r.getTitle(), r.getStatus(),
                r.getFileUrl(), r.getGeneratedAt(), r.getRequestedBy(),
                r.getErrorMessage(), r.getCreatedAt()
        );
    }
}
