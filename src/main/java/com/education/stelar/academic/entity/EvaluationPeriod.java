package com.education.stelar.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "evaluation_periods")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvaluationPeriod extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriod academicPeriod;

    public static EvaluationPeriod create(String name, BigDecimal weight, int sequenceOrder) {
        EvaluationPeriod ep = new EvaluationPeriod();
        ep.name = name;
        ep.weight = weight;
        ep.sequenceOrder = sequenceOrder;
        return ep;
    }

    public static EvaluationPeriod create(String name, BigDecimal weight, int sequenceOrder,
                                          LocalDate startDate, LocalDate endDate) {
        EvaluationPeriod ep = create(name, weight, sequenceOrder);
        ep.startDate = startDate;
        ep.endDate = endDate;
        return ep;
    }

    void assignToAcademicPeriod(AcademicPeriod ap) {
        this.academicPeriod = ap;
    }
}
