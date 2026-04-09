package com.niyamitra.common.exception;

import java.time.Instant;

public record NiyamitraApiError(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    public NiyamitraApiError(int status, String error, String message, String path) {
        this(status, error, message, path, Instant.now());
    }
}
