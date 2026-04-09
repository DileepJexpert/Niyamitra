package com.niyamitra.kavach.notification.controller;

import com.niyamitra.kavach.notification.model.KavachNotification;
import com.niyamitra.kavach.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kavach/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public List<KavachNotification> listNotifications(@RequestParam UUID tenantId) {
        return notificationRepository.findByTenantIdOrderBySentAtDesc(tenantId);
    }

    @GetMapping("/{id}")
    public KavachNotification getNotification(@PathVariable UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
    }
}
