package com.education.stelar.academic.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.education.stelar.academic.entity.EvaluationPeriod;

public record EvaluationPeriodResponse(
        UUID id,
        String name,
        BigDecimal weight,
        Integer sequenceOrder,
        LocalDate startDate,
        LocalDate endDate
) {
    public static EvaluationPeriodResponse from(EvaluationPeriod ep) {
        return new EvaluationPeriodResponse(
                ep.getId(),
                ep.getName(),
                ep.getWeight(),
                ep.getSequenceOrder(),
                ep.getStartDate(),
                ep.getEndDate()
        );
    }
}
