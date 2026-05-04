package com.education.stelar.analytics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(columnDefinition = "text")
    private String parameters;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    public static Report create(ReportType reportType, String title, UUID requestedBy, String parameters) {
        Report r = new Report();
        r.reportType = reportType;
        r.title = title;
        r.requestedBy = requestedBy;
        r.parameters = parameters;
        r.status = ReportStatus.PENDING;
        return r;
    }

    public void markGenerating() {
        this.status = ReportStatus.GENERATING;
    }

    public void markCompleted(String fileUrl) {
        this.status = ReportStatus.COMPLETED;
        this.fileUrl = fileUrl;
        this.generatedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        this.errorMessage = errorMessage;
        this.generatedAt = Instant.now();
    }
}
