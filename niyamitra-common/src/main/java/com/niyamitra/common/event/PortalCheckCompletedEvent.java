package com.niyamitra.common.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortalCheckCompletedEvent(
        UUID eventId,
        UUID tenantId,
        String portalName,
        String status,
        List<String> downloadedFiles,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public PortalCheckCompletedEvent {
        if (brandSource == null) brandSource = "KAVACH";
    }
}
