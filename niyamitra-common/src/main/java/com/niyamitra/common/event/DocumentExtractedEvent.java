package com.niyamitra.common.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DocumentExtractedEvent(
        UUID eventId,
        UUID documentId,
        UUID taskId,
        UUID tenantId,
        Map<String, Object> extractedData,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public DocumentExtractedEvent {
        if (brandSource == null) brandSource = "KAVACH";
    }
}
