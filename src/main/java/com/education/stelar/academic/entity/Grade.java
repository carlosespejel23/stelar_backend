package com.education.stelar.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "grades",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_grade_enrollment_period",
                columnNames = {"enrollment_id", "evaluation_period_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_period_id", nullable = false)
    private EvaluationPeriod evaluationPeriod;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(length = 500)
    private String remarks;

    @Column(name = "recorded_by", nullable = false)
    private UUID recordedBy;

    public static Grade record(Enrollment enrollment, EvaluationPeriod evaluationPeriod,
                               BigDecimal score, UUID recordedBy) {
        Grade g = new Grade();
        g.enrollment = enrollment;
        g.evaluationPeriod = evaluationPeriod;
        g.score = score;
        g.recordedBy = recordedBy;
        return g;
    }

    public void updateScore(BigDecimal newScore, String remarks) {
        this.score = newScore;
        this.remarks = remarks;
    }

    public boolean isPassing(BigDecimal passingGrade) {
        return this.score.compareTo(passingGrade) >= 0;
    }
}
