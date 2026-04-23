package com.casavo.leadrouting.leadrouting.domain.service;

import com.casavo.leadrouting.leadrouting.domain.model.Agent;

import java.util.Objects;

public record AgentWithLoad(Agent agent, int currentLoad) {
    public AgentWithLoad {
        Objects.requireNonNull(agent);
        if (currentLoad < 0) throw new IllegalArgumentException("currentLoad must be non-negative");
    }
}
