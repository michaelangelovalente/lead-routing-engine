package com.casavo.leadrouting.leadrouting.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class Assignment {

    private final AssignmentId id;
    private final LeadId leadId;
    private final AgentId agentId;
    private final Instant assignedAt;

    public Assignment(AssignmentId id, LeadId leadId, AgentId agentId, Instant assignedAt) {
        this.id = Objects.requireNonNull(id);
        this.leadId = Objects.requireNonNull(leadId);
        this.agentId = Objects.requireNonNull(agentId);
        this.assignedAt = Objects.requireNonNull(assignedAt);
    }

    public AssignmentId id() { return id; }
    public LeadId leadId() { return leadId; }
    public AgentId agentId() { return agentId; }
    public Instant assignedAt() { return assignedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignment a)) return false;
        return Objects.equals(id, a.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
