package com.niyamitra.common.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortalCheckRequestedEvent(
        UUID eventId,
        UUID tenantId,
        String portalName,
        UUID credentialId,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public PortalCheckRequestedEvent {
        if (brandSource == null) brandSource = "KAVACH";
    }
}
