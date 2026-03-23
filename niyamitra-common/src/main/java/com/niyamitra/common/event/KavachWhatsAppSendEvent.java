package com.niyamitra.common.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record KavachWhatsAppSendEvent(
        UUID eventId,
        String toPhone,
        String templateId,
        Map<String, String> parameters,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public KavachWhatsAppSendEvent {
        if (brandSource == null) brandSource = "KAVACH";
    }
}
