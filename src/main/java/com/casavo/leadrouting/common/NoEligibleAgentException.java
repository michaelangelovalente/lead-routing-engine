package com.casavo.leadrouting.common;

import com.casavo.leadrouting.leadrouting.domain.model.City;
import com.casavo.leadrouting.leadrouting.domain.model.LeadId;

public class NoEligibleAgentException extends RuntimeException {

    private final LeadId leadId;
    private final City city;

    public NoEligibleAgentException(LeadId leadId, City city) {
        super("No active agent in city '" + city.name() + "' has capacity at this time.");
        this.leadId = leadId;
        this.city = city;
    }

    public LeadId leadId() { return leadId; }
    public City city() { return city; }
}
