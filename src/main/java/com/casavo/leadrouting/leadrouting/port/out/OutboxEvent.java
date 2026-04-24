package com.casavo.leadrouting.leadrouting.port.out;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        String eventType,
        String payload,
        Instant createdAt,
        int retryCount
) {}
