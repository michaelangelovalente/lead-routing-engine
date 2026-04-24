package com.casavo.leadrouting.common;

import com.casavo.leadrouting.leadrouting.domain.model.AgentId;

public class AgentNotFoundException extends RuntimeException {

    private final AgentId agentId;

    public AgentNotFoundException(AgentId agentId) {
        super("No agent exists with ID '" + agentId.value() + "'.");
        this.agentId = agentId;
    }

    public AgentId agentId() { return agentId; }
}
