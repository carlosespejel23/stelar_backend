package com.education.stelar.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "academic_periods",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_academic_period_tenant_name",
                columnNames = {"tenant_id", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AcademicPeriod extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "academicPeriod", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<EvaluationPeriod> evaluationPeriods = new ArrayList<>();

    public static AcademicPeriod create(String name, PeriodType periodType,
                                        LocalDate startDate, LocalDate endDate) {
        AcademicPeriod ap = new AcademicPeriod();
        ap.name = name.trim();
        ap.periodType = periodType;
        ap.startDate = startDate;
        ap.endDate = endDate;
        return ap;
    }

    public void addEvaluationPeriod(EvaluationPeriod ep) {
        ep.assignToAcademicPeriod(this);
        this.evaluationPeriods.add(ep);
    }

    public void update(String name, LocalDate startDate, LocalDate endDate) {
        if (name != null && !name.isBlank()) this.name = name.trim();
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean areWeightsValid() {
        if (evaluationPeriods.isEmpty()) return false;
        BigDecimal total = evaluationPeriods.stream()
                .map(EvaluationPeriod::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.compareTo(new BigDecimal("100")) == 0;
    }

    public int getTotalWeeks() {
        return (int) ChronoUnit.WEEKS.between(startDate, endDate);
    }

    public int getCurrentWeek() {
        LocalDate now = LocalDate.now();
        if (now.isBefore(startDate)) return 0;
        if (now.isAfter(endDate)) return getTotalWeeks();
        return (int) ChronoUnit.WEEKS.between(startDate, now);
    }
}
