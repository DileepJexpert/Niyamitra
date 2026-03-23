package com.niyamitra.kavach.agent.service;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.event.KavachWhatsAppReceivedEvent;
import com.niyamitra.common.event.KavachWhatsAppSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KavachMessageConsumer {

    private final KavachAgent kavachAgent;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = KafkaTopics.WHATSAPP_RECEIVED, groupId = "kavach-floor-manager")
    public void handleIncomingMessage(KavachWhatsAppReceivedEvent event) {
        log.info("Kavach processing message from {}: {}", event.fromPhone(), event.content());

        try {
            // Process through Kavach AI agent
            String response = kavachAgent.chat(event.content(), event.fromPhone());

            // Publish reply to Kavach notification service
            KavachWhatsAppSendEvent sendEvent = new KavachWhatsAppSendEvent(
                    UUID.randomUUID(),
                    event.fromPhone(),
                    null, // text message, not template
                    Map.of("body", response),
                    Instant.now(),
                    event.correlationId(),
                    "KAVACH"
            );
            kafkaTemplate.send(KafkaTopics.WHATSAPP_SEND, event.fromPhone(), sendEvent);

            log.info("Kavach replied to {}: {}", event.fromPhone(), response);
        } catch (Exception e) {
            log.error("Kavach agent error processing message from {}: {}", event.fromPhone(), e.getMessage(), e);
        }
    }
}
