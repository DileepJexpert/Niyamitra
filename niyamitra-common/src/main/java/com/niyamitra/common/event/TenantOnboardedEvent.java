package com.niyamitra.common.event;

import com.niyamitra.common.enums.IndustryCategory;
import java.time.Instant;
import java.util.UUID;

public record TenantOnboardedEvent(
        UUID eventId,
        UUID tenantId,
        String nicCode,
        String state,
        IndustryCategory industryCategory,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public TenantOnboardedEvent {
        if (brandSource == null) brandSource = "NIYAMITRA";
    }
}
