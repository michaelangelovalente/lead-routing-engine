package com.casavo.leadrouting.leadrouting.domain.event;

public record LeadAssignedEvent(
        String eventId,
        String leadId,
        String agentId,
        String assignmentId,
        String city,
        String assignedAt,
        // version handling for code change/structure event change (if fields are added change schema version for deserialazion)
        int schemaVersion

) implements DomainEvent {}
