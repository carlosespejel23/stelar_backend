package com.education.stelar.analytics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "academic_alerts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicAlert extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "academic_period_id", nullable = false)
    private UUID academicPeriodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "factor_scores", columnDefinition = "text")
    private String factorScores;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(name = "dismissed_by")
    private UUID dismissedBy;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @Column(name = "dismiss_reason", length = 500)
    private String dismissReason;

    public static AcademicAlert create(UUID studentId, UUID academicPeriodId,
                                       RiskLevel riskLevel, int riskScore, String factorScores) {
        AcademicAlert alert = new AcademicAlert();
        alert.studentId = studentId;
        alert.academicPeriodId = academicPeriodId;
        alert.riskLevel = riskLevel;
        alert.riskScore = riskScore;
        alert.factorScores = factorScores;
        alert.status = AlertStatus.ACTIVE;
        return alert;
    }

    public void updateRisk(RiskLevel riskLevel, int riskScore, String factorScores) {
        this.riskLevel = riskLevel;
        this.riskScore = riskScore;
        this.factorScores = factorScores;
    }

    public void dismiss(UUID dismissedBy, String reason) {
        this.status = AlertStatus.DISMISSED;
        this.dismissedBy = dismissedBy;
        this.dismissedAt = Instant.now();
        this.dismissReason = reason;
    }

    public void resolve() {
        this.status = AlertStatus.RESOLVED;
    }
}
