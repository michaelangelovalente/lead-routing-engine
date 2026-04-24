package com.casavo.leadrouting.leadrouting.port.out;

import com.casavo.leadrouting.common.NotificationDeliveryException;
import com.casavo.leadrouting.leadrouting.domain.event.LeadAssignedEvent;

public interface NotificationPort {
    void notify(LeadAssignedEvent event) throws NotificationDeliveryException;
}
