package com.education.stelar.analytics.service;

import java.util.Map;

import com.education.stelar.analytics.entity.RiskLevel;

public record RiskResult(
        int totalScore,
        RiskLevel level,
        Map<String, Integer> factorScores
) {
}
