package com.casavo.leadrouting.leadrouting.application.query;

import com.casavo.leadrouting.leadrouting.domain.model.AgentId;

import java.util.Optional;

public interface ListAssignmentsUseCase {
    AssignmentPage listByAgent(AgentId agentId, Optional<Cursor> cursor, int limit);
}
