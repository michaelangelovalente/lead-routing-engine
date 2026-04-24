package com.casavo.leadrouting.leadrouting.port.out;

import com.casavo.leadrouting.leadrouting.domain.event.LeadAssignedEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {
    void append(LeadAssignedEvent event);
    List<OutboxEvent> findUnpublished(int maxBatchSize, int maxRetries);
    void markPublished(UUID eventId, Instant publishedAt);
    void incrementRetryCount(UUID eventId, String lastError);
    void markFailed(UUID eventId, Instant failedAt, String lastError);
}
