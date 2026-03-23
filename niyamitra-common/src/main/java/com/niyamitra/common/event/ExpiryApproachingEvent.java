package com.niyamitra.common.event;

import java.time.Instant;
import java.util.UUID;

public record ExpiryApproachingEvent(
        UUID eventId,
        UUID taskId,
        UUID tenantId,
        UUID userId,
        int daysRemaining,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public ExpiryApproachingEvent {
        if (brandSource == null) brandSource = "KAVACH";
    }
}
