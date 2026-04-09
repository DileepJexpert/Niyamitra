package com.niyamitra.kavach.notification.service;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.event.EscalationTriggeredEvent;
import com.niyamitra.common.event.ExpiryApproachingEvent;
import com.niyamitra.common.event.GazetteFoundEvent;
import com.niyamitra.common.event.KavachWhatsAppSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KavachEventConsumer {

    private final WhatsAppService whatsAppService;
    private final UserLookupService userLookupService;

    @KafkaListener(topics = KafkaTopics.WHATSAPP_SEND, groupId = "kavach-notification-service")
    public void handleWhatsAppSend(KavachWhatsAppSendEvent event) {
        log.info("Kavach dispatching WhatsApp message to {}, template={}", event.toPhone(), event.templateId());
        if (event.templateId() != null) {
            whatsAppService.sendTemplateMessage(null, null, event.toPhone(), event.templateId(), event.parameters());
        } else {
            String body = event.parameters() != null ? event.parameters().getOrDefault("body", "") : "";
            whatsAppService.sendTextMessage(null, null, event.toPhone(), body);
        }
    }

    @KafkaListener(topics = KafkaTopics.EXPIRY_APPROACHING, groupId = "kavach-notification-service")
    public void handleExpiryApproaching(ExpiryApproachingEvent event) {
        log.info("Kavach expiry alert: task={}, daysRemaining={}", event.taskId(), event.daysRemaining());

        try {
            String phone = userLookupService.getPhoneByUserId(event.userId());
            if (phone == null) {
                log.warn("No phone found for user {}, skipping notification", event.userId());
                return;
            }

            String templateId;
            if (event.daysRemaining() <= 5) {
                templateId = "KVH-002"; // kavach_reminder_5d
            } else {
                templateId = "KVH-001"; // kavach_reminder_30d
            }

            whatsAppService.sendTemplateMessage(
                    event.tenantId(), event.userId(), phone, templateId,
                    Map.of("days", String.valueOf(event.daysRemaining()), "taskId", event.taskId().toString())
            );
        } catch (Exception e) {
            log.error("Failed to send expiry notification for task {}: {}", event.taskId(), e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.ESCALATION_TRIGGERED, groupId = "kavach-notification-service")
    public void handleEscalation(EscalationTriggeredEvent event) {
        log.info("Kavach escalation: task={}, reason={}", event.taskId(), event.reason());

        try {
            // Look up owner phone for this tenant
            String ownerPhone = userLookupService.getOwnerPhoneByTenantId(event.tenantId());
            if (ownerPhone == null) {
                log.warn("No owner phone found for tenant {}", event.tenantId());
                return;
            }

            whatsAppService.sendTemplateMessage(
                    event.tenantId(), event.escalateToUserId(), ownerPhone,
                    "KVH-003", // kavach_escalation
                    Map.of("reason", event.reason(), "taskId", event.taskId().toString())
            );
        } catch (Exception e) {
            log.error("Failed to send escalation for task {}: {}", event.taskId(), e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.GAZETTE_FOUND, groupId = "kavach-notification-service")
    public void handleGazetteFound(GazetteFoundEvent event) {
        log.info("Kavach gazette alert: notification={}, affecting {} tenants",
                event.notificationId(), event.affectedTenantIds().size());

        for (var tenantId : event.affectedTenantIds()) {
            try {
                String ownerPhone = userLookupService.getOwnerPhoneByTenantId(tenantId);
                if (ownerPhone != null) {
                    whatsAppService.sendTemplateMessage(
                            tenantId, null, ownerPhone,
                            "KVH-004", // kavach_gazette_alert
                            Map.of("notificationId", event.notificationId().toString())
                    );
                }
            } catch (Exception e) {
                log.error("Failed to send gazette notification to tenant {}: {}", tenantId, e.getMessage());
            }
        }
    }
}
