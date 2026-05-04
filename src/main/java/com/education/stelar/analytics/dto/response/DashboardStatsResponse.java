package com.education.stelar.analytics.dto.response;

import java.util.List;

public record DashboardStatsResponse(
        long totalStudents,
        long totalActiveAlerts,
        long criticalAlerts,
        long highAlerts,
        long mediumAlerts,
        long lowAlerts,
        long unreadNotifications,
        List<GroupStatsResponse> groupStats
) {
}
