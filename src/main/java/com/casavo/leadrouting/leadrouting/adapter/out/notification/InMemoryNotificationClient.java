package com.casavo.leadrouting.leadrouting.adapter.out.notification;

import com.casavo.leadrouting.common.NotificationDeliveryException;
import com.casavo.leadrouting.leadrouting.domain.event.LeadAssignedEvent;
import com.casavo.leadrouting.leadrouting.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class InMemoryNotificationClient implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(InMemoryNotificationClient.class);

    private final double failureRate;

    public InMemoryNotificationClient(@Value("${notification.failure-rate:0.0}") double failureRate) {
        this.failureRate = failureRate;
    }

    @Override
    public void notify(LeadAssignedEvent event) throws NotificationDeliveryException {
        if (failureRate > 0.0 && ThreadLocalRandom.current().nextDouble() < failureRate) {
            throw new NotificationDeliveryException(
                    "Simulated notification failure for event " + event.eventId());
        }
        log.info("Notification sent: leadId={} agentId={} city={}",
                event.leadId(), event.agentId(), event.city());
    }
}
