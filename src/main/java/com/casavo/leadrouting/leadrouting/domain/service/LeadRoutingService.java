package com.casavo.leadrouting.leadrouting.domain.service;

import com.casavo.leadrouting.leadrouting.domain.model.Agent;
import com.casavo.leadrouting.leadrouting.domain.model.Lead;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public final class LeadRoutingService {

    private final Random tiebreaker;

    public LeadRoutingService(Random tiebreaker) {
        this.tiebreaker = Objects.requireNonNull(tiebreaker);
    }

    public Optional<Agent> pickAgent(Lead lead, List<AgentWithLoad> candidates) {
        Objects.requireNonNull(lead);
        if (candidates.isEmpty()) return Optional.empty();

        int minLoad = candidates.stream()
                .mapToInt(AgentWithLoad::currentLoad)
                .min().orElseThrow();

        List<Agent> lowestLoaded = candidates.stream()
                .filter(c -> c.currentLoad() == minLoad)
                .map(AgentWithLoad::agent)
                .toList();

        return Optional.of(lowestLoaded.get(tiebreaker.nextInt(lowestLoaded.size())));
    }
}
