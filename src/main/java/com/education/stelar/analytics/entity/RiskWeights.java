package com.education.stelar.analytics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.education.stelar.kernel.persistence.TenantAwareEntity;

@Entity
@Table(name = "risk_weights")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiskWeights extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "low_average", nullable = false)
    private double lowAverage = 0.30;

    @Column(name = "negative_trend", nullable = false)
    private double negativeTrend = 0.15;

    @Column(name = "low_attendance", nullable = false)
    private double lowAttendance = 0.20;

    @Column(name = "consecutive_absences", nullable = false)
    private double consecutiveAbsences = 0.10;

    @Column(name = "failed_subjects", nullable = false)
    private double failedSubjects = 0.15;

    @Column(name = "temporal_urgency", nullable = false)
    private double temporalUrgency = 0.10;

    public static RiskWeights createDefault() {
        RiskWeights w = new RiskWeights();
        w.lowAverage = 0.30;
        w.negativeTrend = 0.15;
        w.lowAttendance = 0.20;
        w.consecutiveAbsences = 0.10;
        w.failedSubjects = 0.15;
        w.temporalUrgency = 0.10;
        return w;
    }

    public void update(double lowAverage, double negativeTrend, double lowAttendance,
                       double consecutiveAbsences, double failedSubjects, double temporalUrgency) {
        this.lowAverage = lowAverage;
        this.negativeTrend = negativeTrend;
        this.lowAttendance = lowAttendance;
        this.consecutiveAbsences = consecutiveAbsences;
        this.failedSubjects = failedSubjects;
        this.temporalUrgency = temporalUrgency;
    }

    public boolean isValid() {
        double sum = lowAverage + negativeTrend + lowAttendance
                + consecutiveAbsences + failedSubjects + temporalUrgency;
        return Math.abs(sum - 1.0) < 0.001;
    }
}
