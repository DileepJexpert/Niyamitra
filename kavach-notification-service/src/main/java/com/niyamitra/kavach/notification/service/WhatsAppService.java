package com.niyamitra.kavach.notification.service;

import com.niyamitra.kavach.notification.model.KavachNotification;
import com.niyamitra.kavach.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final NotificationRepository notificationRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${kavach.whatsapp.api-url:https://graph.facebook.com/v21.0}")
    private String apiUrl;

    @Value("${kavach.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${kavach.whatsapp.access-token:}")
    private String accessToken;

    public void sendTemplateMessage(UUID tenantId, UUID userId, String toPhone,
                                     String templateId, Map<String, String> parameters) {
        KavachNotification notification = KavachNotification.builder()
                .tenantId(tenantId)
                .userId(userId)
                .channel("WHATSAPP")
                .templateId(templateId)
                .recipient(toPhone)
                .messageContent(parameters.toString())
                .build();

        notification = notificationRepository.save(notification);

        try {
            // Meta WhatsApp Cloud API call
            Map<String, Object> payload = Map.of(
                    "messaging_product", "whatsapp",
                    "to", toPhone,
                    "type", "template",
                    "template", Map.of(
                            "name", templateId,
                            "language", Map.of("code", "hi"),
                            "components", parameters
                    )
            );

            log.info("Kavach sending WhatsApp template {} to {}", templateId, toPhone);

            // In production, this would make the actual API call
            // For now, log and mark as sent
            notification.setDeliveryStatus("SENT");
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("Kavach WhatsApp send failed for {}: {}", toPhone, e.getMessage());
            notification.setDeliveryStatus("FAILED");
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        }
    }

    public void sendTextMessage(UUID tenantId, UUID userId, String toPhone, String message) {
        KavachNotification notification = KavachNotification.builder()
                .tenantId(tenantId)
                .userId(userId)
                .channel("WHATSAPP")
                .recipient(toPhone)
                .messageContent(message)
                .build();

        notification = notificationRepository.save(notification);

        log.info("Kavach sending text message to {}: {}", toPhone, message);
        notification.setDeliveryStatus("SENT");
        notification.setSentAt(Instant.now());
        notificationRepository.save(notification);
    }
}
