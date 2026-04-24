package com.casavo.leadrouting.leadrouting.adapter.out.outbox;

import com.casavo.leadrouting.common.NotificationDeliveryException;
import com.casavo.leadrouting.leadrouting.domain.event.LeadAssignedEvent;
import com.casavo.leadrouting.leadrouting.port.out.NotificationPort;
import com.casavo.leadrouting.leadrouting.port.out.OutboxEvent;
import com.casavo.leadrouting.leadrouting.port.out.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRIES = 5;

    private final OutboxRepository outboxRepo;
    private final NotificationPort notificationPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OutboxRelay(
            OutboxRepository outboxRepo,
            NotificationPort notificationPort,
            ObjectMapper objectMapper,
            Clock clock) {
        this.outboxRepo = outboxRepo;
        this.notificationPort = notificationPort;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void relay() {
        List<OutboxEvent> batch = outboxRepo.findUnpublished(BATCH_SIZE, MAX_RETRIES);
        for (OutboxEvent event : batch) {
            try {
                LeadAssignedEvent domainEvent = objectMapper.readValue(event.payload(), LeadAssignedEvent.class);
                notificationPort.notify(domainEvent);
                outboxRepo.markPublished(event.id(), clock.instant());
            } catch (NotificationDeliveryException e) {
                int newCount = event.retryCount() + 1;
                if (newCount >= MAX_RETRIES) {
                    outboxRepo.markFailed(event.id(), clock.instant(), e.getMessage());
                    log.error("Event {} permanently failed after {} retries", event.id(), MAX_RETRIES);
                } else {
                    outboxRepo.incrementRetryCount(event.id(), e.getMessage());
                    log.warn("Notification failed for event {}: {}", event.id(), e.getMessage());
                }
            } catch (Exception e) {
                outboxRepo.markFailed(event.id(), clock.instant(), e.getMessage());
                log.error("Corrupt outbox payload for event {}, marking failed", event.id(), e);
            }
        }
    }
}
