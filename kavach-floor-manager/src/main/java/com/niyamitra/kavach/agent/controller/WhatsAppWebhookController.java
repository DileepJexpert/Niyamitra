package com.niyamitra.kavach.agent.controller;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.event.KavachWhatsAppReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/kavach/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class WhatsAppWebhookController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kavach.whatsapp.verify-token:kavach-verify-token}")
    private String verifyToken;

    /**
     * Meta WhatsApp webhook verification (GET)
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Kavach WhatsApp webhook verified");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).body("Verification failed");
    }

    /**
     * Meta WhatsApp webhook incoming messages (POST)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> payload) {
        log.info("Kavach received WhatsApp webhook: {}", payload);

        try {
            // Extract message details from Meta webhook payload
            var entry = ((java.util.List<Map<String, Object>>) payload.get("entry"));
            if (entry == null || entry.isEmpty()) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            var changes = (java.util.List<Map<String, Object>>) entry.get(0).get("changes");
            if (changes == null || changes.isEmpty()) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            var value = (Map<String, Object>) changes.get(0).get("value");
            var messages = (java.util.List<Map<String, Object>>) value.get("messages");
            if (messages == null || messages.isEmpty()) {
                return ResponseEntity.ok("EVENT_RECEIVED");
            }

            var message = messages.get(0);
            String from = (String) message.get("from");
            String type = (String) message.get("type");
            String content = "";
            String mediaUrl = null;

            if ("text".equals(type)) {
                var textObj = (Map<String, Object>) message.get("text");
                content = (String) textObj.get("body");
            } else if ("image".equals(type)) {
                var imageObj = (Map<String, Object>) message.get("image");
                mediaUrl = (String) imageObj.get("id");
                content = "[IMAGE]";
            }

            // Publish to Kafka for Kavach Floor Manager agent processing
            KavachWhatsAppReceivedEvent event = new KavachWhatsAppReceivedEvent(
                    UUID.randomUUID(), from, type, content, mediaUrl,
                    Instant.now(), UUID.randomUUID(), "KAVACH"
            );
            kafkaTemplate.send(KafkaTopics.WHATSAPP_RECEIVED, from, event);
            log.info("Kavach published WhatsApp message from {} to Kafka", from);

        } catch (Exception e) {
            log.error("Error processing Kavach WhatsApp webhook: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
