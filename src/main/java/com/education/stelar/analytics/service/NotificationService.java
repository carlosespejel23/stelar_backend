package com.education.stelar.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.analytics.dto.request.NotificationPreferenceRequest;
import com.education.stelar.analytics.dto.response.InAppNotificationResponse;
import com.education.stelar.analytics.dto.response.NotificationPreferenceResponse;
import com.education.stelar.analytics.entity.*;
import com.education.stelar.analytics.repository.InAppNotificationRepository;
import com.education.stelar.analytics.repository.NotificationPreferenceRepository;
import com.education.stelar.identity.entity.User;
import com.education.stelar.identity.repository.UserRepository;
import com.education.stelar.kernel.email.EmailService;
import com.education.stelar.kernel.exception.ResourceNotFoundException;
import com.education.stelar.kernel.multitenancy.TenantContext;
import com.education.stelar.kernel.pagination.PagedResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final InAppNotificationRepository inAppNotificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public PagedResponse<InAppNotificationResponse> getNotifications(UUID userId, UUID tenantId, Pageable pageable) {
        Page<InAppNotification> page = inAppNotificationRepository
                .findAllByUserIdAndTenantIdOrderByCreatedAtDesc(userId, tenantId, pageable);
        return PagedResponse.from(page, InAppNotificationResponse::from);
    }

    public long getUnreadCount(UUID userId, UUID tenantId) {
        return inAppNotificationRepository.countByUserIdAndTenantIdAndReadFalse(userId, tenantId);
    }

    @Transactional
    public InAppNotificationResponse markRead(UUID notificationId, UUID userId) {
        InAppNotification notification = inAppNotificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        notification.markRead();
        return InAppNotificationResponse.from(inAppNotificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(UUID userId, UUID tenantId) {
        inAppNotificationRepository.markAllReadByUserAndTenant(userId, tenantId);
    }

    public NotificationPreferenceResponse getPreferences(UUID userId, UUID tenantId) {
        NotificationPreference pref = notificationPreferenceRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> NotificationPreference.createDefault(userId));
        return NotificationPreferenceResponse.from(pref);
    }

    @Transactional
    public NotificationPreferenceResponse updatePreferences(UUID userId, UUID tenantId,
                                                             NotificationPreferenceRequest request) {
        NotificationPreference pref = notificationPreferenceRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> NotificationPreference.createDefault(userId));
        pref.update(request.alertLevelThreshold(), request.emailEnabled(),
                request.inAppEnabled(), request.emailFrequency());
        return NotificationPreferenceResponse.from(notificationPreferenceRepository.save(pref));
    }

    @Transactional
    public void notifyAlert(AcademicAlert alert, UUID tenantId) {
        List<NotificationPreference> preferences = notificationPreferenceRepository.findAllByTenantId(tenantId);
        for (NotificationPreference pref : preferences) {
            if (!pref.shouldNotifyForLevel(alert.getRiskLevel())) continue;
            if (pref.isInAppEnabled()) {
                sendInApp(pref.getUserId(), tenantId, alert);
            }
            if (pref.isEmailEnabled() && pref.getEmailFrequency() == EmailFrequency.IMMEDIATE) {
                sendImmediateEmail(pref.getUserId(), alert);
            }
        }
    }

    private void sendInApp(UUID userId, UUID tenantId, AcademicAlert alert) {
        String title = buildTitle(alert.getRiskLevel());
        String message = buildMessage(alert);
        TenantContext.setCurrentTenant(tenantId);
        InAppNotification notification = InAppNotification.create(userId, title, message,
                alert.getRiskLevel(), alert.getId());
        inAppNotificationRepository.save(notification);
    }

    private void sendImmediateEmail(UUID userId, AcademicAlert alert) {
        userRepository.findById(userId).ifPresent(user -> {
            String subject = "Alerta académica: " + alert.getRiskLevel().name();
            String body = buildEmailBody(user, alert);
            emailService.send(user.getEmail(), subject, body);
        });
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendDailyDigest() {
        log.info("Enviando digest diario de notificaciones...");
        List<NotificationPreference> preferences = notificationPreferenceRepository
                .findAllByEmailEnabledTrueAndEmailFrequency(EmailFrequency.DAILY_DIGEST);
        for (NotificationPreference pref : preferences) {
            userRepository.findById(pref.getUserId()).ifPresent(user -> {
                List<InAppNotification> unread = inAppNotificationRepository
                        .findAllByUserIdAndTenantIdAndReadFalse(pref.getUserId(), pref.getTenantId());
                if (!unread.isEmpty()) {
                    String body = buildDigestBody(user, unread, "diario");
                    emailService.send(user.getEmail(), "Resumen diario de alertas académicas", body);
                }
            });
        }
    }

    @Scheduled(cron = "0 0 8 * * MON")
    @Transactional
    public void sendWeeklyDigest() {
        log.info("Enviando digest semanal de notificaciones...");
        List<NotificationPreference> preferences = notificationPreferenceRepository
                .findAllByEmailEnabledTrueAndEmailFrequency(EmailFrequency.WEEKLY_DIGEST);
        for (NotificationPreference pref : preferences) {
            userRepository.findById(pref.getUserId()).ifPresent(user -> {
                List<InAppNotification> unread = inAppNotificationRepository
                        .findAllByUserIdAndTenantIdAndReadFalse(pref.getUserId(), pref.getTenantId());
                if (!unread.isEmpty()) {
                    String body = buildDigestBody(user, unread, "semanal");
                    emailService.send(user.getEmail(), "Resumen semanal de alertas académicas", body);
                }
            });
        }
    }

    private String buildTitle(RiskLevel level) {
        return switch (level) {
            case LOW -> "Alerta de riesgo bajo detectada";
            case MEDIUM -> "Alerta de riesgo medio detectada";
            case HIGH -> "Alerta de riesgo alto detectada";
            case CRITICAL -> "⚠ Alerta crítica detectada";
        };
    }

    private String buildMessage(AcademicAlert alert) {
        return "Se detectó un estudiante con riesgo académico nivel "
                + alert.getRiskLevel().name()
                + " (puntaje: " + alert.getRiskScore() + "/100).";
    }

    private String buildEmailBody(User user, AcademicAlert alert) {
        return "<p>Hola " + user.getFirstName() + ",</p>"
                + "<p>Se ha detectado una alerta académica de nivel <strong>"
                + alert.getRiskLevel().name() + "</strong> con puntaje "
                + alert.getRiskScore() + "/100.</p>"
                + "<p>Ingresa a la plataforma Stellar para más detalles.</p>";
    }

    private String buildDigestBody(User user, List<InAppNotification> notifications, String period) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>Hola ").append(user.getFirstName()).append(",</p>");
        sb.append("<p>Tienes <strong>").append(notifications.size())
                .append("</strong> notificaciones sin leer en tu resumen ").append(period).append(":</p><ul>");
        notifications.forEach(n -> sb.append("<li>").append(n.getTitle()).append(": ")
                .append(n.getMessage()).append("</li>"));
        sb.append("</ul><p>Ingresa a Stellar para más detalles.</p>");
        return sb.toString();
    }
}
