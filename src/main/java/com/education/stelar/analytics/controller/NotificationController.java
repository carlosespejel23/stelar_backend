package com.education.stelar.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.analytics.dto.request.NotificationPreferenceRequest;
import com.education.stelar.analytics.dto.response.InAppNotificationResponse;
import com.education.stelar.analytics.dto.response.NotificationPreferenceResponse;
import com.education.stelar.analytics.service.NotificationService;
import com.education.stelar.kernel.pagination.PagedResponse;
import com.education.stelar.kernel.security.CurrentUser;
import com.education.stelar.kernel.security.UserPrincipal;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notificaciones in-app y preferencias")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Listar notificaciones in-app del usuario autenticado")
    public ResponseEntity<PagedResponse<InAppNotificationResponse>> getNotifications(
            @CurrentUser UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(
                principal.getId(), principal.getTenantId(), pageable));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Obtener conteo de notificaciones no leídas")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@CurrentUser UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getId(), principal.getTenantId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marcar una notificación como leída")
    public ResponseEntity<InAppNotificationResponse> markRead(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.markRead(id, principal.getId()));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Marcar todas las notificaciones del usuario como leídas")
    public ResponseEntity<Void> markAllRead(@CurrentUser UserPrincipal principal) {
        notificationService.markAllRead(principal.getId(), principal.getTenantId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    @Operation(summary = "Obtener preferencias de notificación del usuario")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getPreferences(
                principal.getId(), principal.getTenantId()));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Actualizar preferencias de notificación")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @CurrentUser UserPrincipal principal,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        return ResponseEntity.ok(notificationService.updatePreferences(
                principal.getId(), principal.getTenantId(), request));
    }
}
