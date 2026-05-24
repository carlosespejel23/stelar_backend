package com.education.stelar.analytics.entity;

public enum RiskLevel {
    LOW,      // 0-24
    MEDIUM,   // 25-49
    HIGH,     // 50-74
    CRITICAL; // 75-100

    public static RiskLevel fromScore(int score) {
        if (score < 25) return LOW;
        if (score < 50) return MEDIUM;
        if (score < 75) return HIGH;
        return CRITICAL;
    }
}
