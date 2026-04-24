package com.casavo.leadrouting.leadrouting.adapter.in.rest.dto;

import com.casavo.leadrouting.leadrouting.domain.model.Assignment;
import com.casavo.leadrouting.leadrouting.domain.model.Lead;
import com.casavo.leadrouting.leadrouting.domain.model.LeadStatus;

import java.util.UUID;

public record LeadResponse(UUID leadId, AssignmentResponse assignment, LeadStatus status) {

    public static LeadResponse from(Lead lead, Assignment assignment) {
        return new LeadResponse(
                lead.id().value(),
                AssignmentResponse.from(assignment),
                lead.status());
    }
}
