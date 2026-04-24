package com.casavo.leadrouting.leadrouting.application.command;

import com.casavo.leadrouting.leadrouting.domain.model.Assignment;
import com.casavo.leadrouting.leadrouting.domain.model.City;
import com.casavo.leadrouting.leadrouting.domain.model.Lead;
import com.casavo.leadrouting.leadrouting.domain.model.LeadId;

public sealed interface AssignmentResult
        permits AssignmentResult.Assigned,
                AssignmentResult.AlreadyAssigned,
                AssignmentResult.NoEligibleAgent {

    record Assigned(Lead lead, Assignment assignment) implements AssignmentResult {}
    record AlreadyAssigned(Lead lead, Assignment assignment) implements AssignmentResult {}
    record NoEligibleAgent(LeadId leadId, City city) implements AssignmentResult {}
}
