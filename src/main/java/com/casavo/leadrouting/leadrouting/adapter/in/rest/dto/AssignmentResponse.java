package com.casavo.leadrouting.leadrouting.adapter.in.rest.dto;

import com.casavo.leadrouting.leadrouting.domain.model.Assignment;

import java.time.Instant;
import java.util.UUID;

public record AssignmentResponse(UUID assignmentId, UUID agentId, Instant assignedAt) {

    public static AssignmentResponse from(Assignment assignment) {
        return new AssignmentResponse(
                assignment.id().value(),
                assignment.agentId().value(),
                assignment.assignedAt());
    }
}
