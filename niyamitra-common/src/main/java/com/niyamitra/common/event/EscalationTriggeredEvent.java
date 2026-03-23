package com.niyamitra.common.event;

import java.time.Instant;
import java.util.UUID;

public record EscalationTriggeredEvent(
        UUID eventId,
        UUID taskId,
        UUID tenantId,
        UUID escalateToUserId,
        String reason,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public EscalationTriggeredEvent {
        if (brandSource == null) brandSource = "KAVACH";
    }
}
