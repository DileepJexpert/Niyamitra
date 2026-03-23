package com.niyamitra.common.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GazetteFoundEvent(
        UUID eventId,
        UUID notificationId,
        List<UUID> affectedTenantIds,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public GazetteFoundEvent {
        if (brandSource == null) brandSource = "ANUPALAN";
    }
}
