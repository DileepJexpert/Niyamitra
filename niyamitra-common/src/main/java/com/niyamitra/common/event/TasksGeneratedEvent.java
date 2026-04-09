package com.niyamitra.common.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TasksGeneratedEvent(
        UUID eventId,
        UUID tenantId,
        List<UUID> applicableRuleIds,
        int tasksCreated,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public TasksGeneratedEvent {
        if (brandSource == null) brandSource = "ANUPALAN";
    }
}
