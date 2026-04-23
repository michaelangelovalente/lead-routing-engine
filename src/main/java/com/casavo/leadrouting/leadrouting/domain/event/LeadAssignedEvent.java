package com.casavo.leadrouting.leadrouting.domain.event;

public record LeadAssignedEvent(
        String eventId,
        String leadId,
        String agentId,
        String assignmentId,
        String city,
        String assignedAt,
        int schemaVersion
) implements DomainEvent {}
