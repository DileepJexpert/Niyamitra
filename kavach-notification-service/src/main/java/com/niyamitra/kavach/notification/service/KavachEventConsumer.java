package com.niyamitra.kavach.notification.service;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.event.EscalationTriggeredEvent;
import com.niyamitra.common.event.ExpiryApproachingEvent;
import com.niyamitra.common.event.KavachWhatsAppSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KavachEventConsumer {

    private final WhatsAppService whatsAppService;

    @KafkaListener(topics = KafkaTopics.WHATSAPP_SEND, groupId = "kavach-notification-service")
    public void handleWhatsAppSend(KavachWhatsAppSendEvent event) {
        log.info("Kavach dispatching WhatsApp message to {}, template={}", event.toPhone(), event.templateId());
        whatsAppService.sendTemplateMessage(
                null, null, event.toPhone(), event.templateId(), event.parameters()
        );
    }

    @KafkaListener(topics = KafkaTopics.EXPIRY_APPROACHING, groupId = "kavach-notification-service")
    public void handleExpiryApproaching(ExpiryApproachingEvent event) {
        log.info("Kavach expiry alert: task={}, daysRemaining={}", event.taskId(), event.daysRemaining());
        // In production: look up user phone, send appropriate template based on daysRemaining
    }

    @KafkaListener(topics = KafkaTopics.ESCALATION_TRIGGERED, groupId = "kavach-notification-service")
    public void handleEscalation(EscalationTriggeredEvent event) {
        log.info("Kavach escalation: task={}, escalateTo={}, reason={}", event.taskId(), event.escalateToUserId(), event.reason());
        // In production: look up owner phone, send KVH-003 escalation template
    }
}
