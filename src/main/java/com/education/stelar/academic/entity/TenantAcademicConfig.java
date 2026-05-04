package com.education.stelar.academic.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "tenant_academic_configs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenantAcademicConfig extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(name = "grading_scale_min", nullable = false, precision = 5, scale = 2)
    private BigDecimal gradingScaleMin;

    @Column(name = "grading_scale_max", nullable = false, precision = 5, scale = 2)
    private BigDecimal gradingScaleMax;

    @Column(name = "passing_grade", nullable = false, precision = 5, scale = 2)
    private BigDecimal passingGrade;

    @Column(name = "attendance_minimum_pct", nullable = false)
    private Integer attendanceMinimumPct;

    public static TenantAcademicConfig createDefault() {
        return createWithPeriodType(PeriodType.SEMESTER);
    }

    public static TenantAcademicConfig createWithPeriodType(PeriodType periodType) {
        TenantAcademicConfig config = new TenantAcademicConfig();
        config.periodType = periodType != null ? periodType : PeriodType.SEMESTER;
        config.gradingScaleMin = new BigDecimal("0");
        config.gradingScaleMax = new BigDecimal("10");
        config.passingGrade = new BigDecimal("6.0");
        config.attendanceMinimumPct = 80;
        return config;
    }

    public void update(PeriodType periodType, BigDecimal gradingScaleMin, BigDecimal gradingScaleMax,
                       BigDecimal passingGrade, Integer attendanceMinimumPct) {
        this.periodType = periodType;
        this.gradingScaleMin = gradingScaleMin;
        this.gradingScaleMax = gradingScaleMax;
        this.passingGrade = passingGrade;
        this.attendanceMinimumPct = attendanceMinimumPct;
    }
}
