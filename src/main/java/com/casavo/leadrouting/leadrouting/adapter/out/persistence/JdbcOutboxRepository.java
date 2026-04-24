package com.casavo.leadrouting.leadrouting.adapter.out.persistence;

import com.casavo.leadrouting.leadrouting.domain.event.LeadAssignedEvent;
import com.casavo.leadrouting.leadrouting.port.out.OutboxEvent;
import com.casavo.leadrouting.leadrouting.port.out.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcOutboxRepository implements OutboxRepository {

    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;

    public JdbcOutboxRepository(JdbcClient jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append(LeadAssignedEvent event) {
        String payload = serialize(event);
        jdbc.sql("""
                INSERT INTO outbox_event (id, event_type, payload, created_at)
                VALUES (:id, :eventType, :payload::jsonb, now())
                """)
                .param("id", UUID.fromString(event.eventId()))
                .param("eventType", "LeadAssigned")
                .param("payload", payload)
                .update();
    }

    @Override
    public List<OutboxEvent> findUnpublished(int maxBatchSize, int maxRetries) {
        return jdbc.sql("""
                SELECT id, event_type, payload::text, created_at, retry_count
                FROM outbox_event
                WHERE published_at IS NULL
                  AND failed_at IS NULL
                  AND retry_count < :maxRetries
                ORDER BY created_at ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
                """)
                .param("batchSize", maxBatchSize)
                .param("maxRetries", maxRetries)
                .query(this::mapRow)
                .list();
    }

    @Override
    public void markPublished(UUID eventId, Instant publishedAt) {
        jdbc.sql("UPDATE outbox_event SET published_at = :publishedAt WHERE id = :id")
                .param("publishedAt", publishedAt)
                .param("id", eventId)
                .update();
    }

    @Override
    public void incrementRetryCount(UUID eventId, String lastError) {
        jdbc.sql("""
                UPDATE outbox_event
                SET retry_count = retry_count + 1, last_error = :lastError
                WHERE id = :id
                """)
                .param("lastError", lastError)
                .param("id", eventId)
                .update();
    }

    @Override
    public void markFailed(UUID eventId, Instant failedAt, String lastError) {
        jdbc.sql("""
                UPDATE outbox_event
                SET failed_at = :failedAt, last_error = :lastError, retry_count = retry_count + 1
                WHERE id = :id
                """)
                .param("failedAt", failedAt)
                .param("lastError", lastError)
                .param("id", eventId)
                .update();
    }

    private String serialize(LeadAssignedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize LeadAssignedEvent", e);
        }
    }

    private OutboxEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new OutboxEvent(
                rs.getObject("id", UUID.class),
                rs.getString("event_type"),
                rs.getString("payload"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getInt("retry_count"));
    }
}
