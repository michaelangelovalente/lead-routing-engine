package com.casavo.leadrouting.leadrouting.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class Agent {

    public static final int MAX_LEADS_PER_WINDOW = 5;
    public static final Duration ASSIGNMENT_WINDOW = Duration.ofHours(24);

    private final AgentId id;
    private final City city;
    private final boolean active;

    public Agent(AgentId id, City city, boolean active) {
        this.id = Objects.requireNonNull(id);
        this.city = Objects.requireNonNull(city);
        this.active = active;
    }

    public Optional<Assignment> tryAssign(Lead lead, int currentLoadInWindow, Instant now) {
        Objects.requireNonNull(lead);
        Objects.requireNonNull(now);
        if (currentLoadInWindow < 0) {
            throw new IllegalArgumentException("currentLoadInWindow must be non-negative");
        }
        if (!active) return Optional.empty();
        if (!city.equals(lead.city())) return Optional.empty();
        if (currentLoadInWindow >= MAX_LEADS_PER_WINDOW) return Optional.empty();
        return Optional.of(new Assignment(AssignmentId.generate(), lead.id(), this.id, now));
    }

    public AgentId id() { return id; }
    public City city() { return city; }
    public boolean active() { return active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agent agent)) return false;
        return Objects.equals(id, agent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
