package com.education.stelar.analytics.dto.response;

import java.util.UUID;

import com.education.stelar.analytics.entity.EmailFrequency;
import com.education.stelar.analytics.entity.NotificationPreference;
import com.education.stelar.analytics.entity.RiskLevel;

public record NotificationPreferenceResponse(
        UUID id,
        UUID userId,
        RiskLevel alertLevelThreshold,
        boolean emailEnabled,
        boolean inAppEnabled,
        EmailFrequency emailFrequency
) {
    public static NotificationPreferenceResponse from(NotificationPreference pref) {
        return new NotificationPreferenceResponse(
                pref.getId(), pref.getUserId(),
                pref.getAlertLevelThreshold(),
                pref.isEmailEnabled(), pref.isInAppEnabled(),
                pref.getEmailFrequency()
        );
    }
}
