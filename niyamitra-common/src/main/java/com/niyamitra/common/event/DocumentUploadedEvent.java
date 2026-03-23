package com.niyamitra.common.event;

import com.niyamitra.common.enums.FileType;
import java.time.Instant;
import java.util.UUID;

public record DocumentUploadedEvent(
        UUID eventId,
        UUID documentId,
        UUID tenantId,
        String s3Key,
        FileType fileType,
        Instant timestamp,
        UUID correlationId,
        String brandSource
) {
    public DocumentUploadedEvent {
        if (brandSource == null) brandSource = "NIYAMITRA";
    }
}
