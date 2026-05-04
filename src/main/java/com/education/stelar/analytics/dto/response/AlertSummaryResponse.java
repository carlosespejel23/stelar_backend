package com.education.stelar.analytics.dto.response;

public record AlertSummaryResponse(
        long low,
        long medium,
        long high,
        long critical,
        long total
) {
}
