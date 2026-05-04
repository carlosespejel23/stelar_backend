package com.education.stelar.analytics.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateRiskWeightsRequest(

        @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
        Double lowAverage,

        @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
        Double negativeTrend,

        @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
        Double lowAttendance,

        @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
        Double consecutiveAbsences,

        @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
        Double failedSubjects,

        @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
        Double temporalUrgency
) {
    public double sum() {
        return lowAverage + negativeTrend + lowAttendance
                + consecutiveAbsences + failedSubjects + temporalUrgency;
    }
}
