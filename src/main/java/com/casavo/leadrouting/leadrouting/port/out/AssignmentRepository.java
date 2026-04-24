package com.casavo.leadrouting.leadrouting.port.out;

import com.casavo.leadrouting.leadrouting.domain.model.AgentId;
import com.casavo.leadrouting.leadrouting.domain.model.Assignment;
import com.casavo.leadrouting.leadrouting.domain.model.LeadId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository {
    void save(Assignment assignment);
    Map<AgentId, Integer> countAssignmentsSincePerAgent(List<AgentId> agentIds, Instant since);
    Optional<Assignment> findByLeadId(LeadId leadId);
    List<Assignment> findByAgentId(AgentId agentId, Optional<Instant> cursorAt, Optional<UUID> cursorId, int maxResults);
}
