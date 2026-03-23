package com.niyamitra.kavach.notification.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kavach_notifications", schema = "kavach_notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KavachNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "channel", length = 20, nullable = false)
    private String channel; // WHATSAPP, SMS, EMAIL

    @Column(name = "template_id", length = 50)
    private String templateId;

    @Column(name = "recipient", length = 100, nullable = false)
    private String recipient;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "delivery_status", length = 20)
    @Builder.Default
    private String deliveryStatus = "PENDING"; // PENDING, SENT, DELIVERED, FAILED

    @Column(name = "whatsapp_message_id")
    private String whatsappMessageId;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;
}
