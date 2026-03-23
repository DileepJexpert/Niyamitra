package com.niyamitra.common.event;

import java.time.Instant;
import java.util.UUID;

public record KavachWhatsAppReceivedEvent(
        UUID eventId,
        String fromPhone,
        String messageType,
        String content,
        String mediaUrl,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public KavachWhatsAppReceivedEvent {
        if (brandSource == null) brandSource = "KAVACH";
    }
}
