package com.casavo.leadrouting.leadrouting.domain;

import com.casavo.leadrouting.leadrouting.domain.model.*;
import com.casavo.leadrouting.leadrouting.domain.service.AgentWithLoad;
import com.casavo.leadrouting.leadrouting.domain.service.LeadRoutingService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Random is injected so tests can seed it for deterministic tiebreaking.
class LeadRoutingServiceTest {

    private final City milano = new City("Milano");
    private final Lead lead = new Lead(
            LeadId.generate(),
            new CustomerContact("Mario Rossi", "mario@example.com", "+39333123"),
            "PROP-MIL-001",
            milano,
            Instant.now(),
            new IdempotencyKey("key-1"));

    @Test
    // Empty list maps to NoEligibleAgent — all agents full or none in city. Must not throw.
    void returnsEmptyWhenNoCandidates() {
        var service = new LeadRoutingService(new Random());
        assertThat(service.pickAgent(lead, List.of())).isEmpty();
    }

    @Test
    // Core business rule: fewest current assignments wins. No ties here, so Random is irrelevant.
    void picksAgentWithLowestLoad() {
        var service = new LeadRoutingService(new Random(0));
        Agent low = agent("low", milano);
        Agent high = agent("high", milano);

        var result = service.pickAgent(lead, List.of(
                new AgentWithLoad(high, 3),
                new AgentWithLoad(low, 1)));

        assertThat(result.map(AgentWithLoad::agent)).contains(low);
    }

    @Test
    // Service receives pre-filtered candidates; a single high-load agent is valid input and must be returned.
    void picksSingleCandidateRegardlessOfLoad() {
        var service = new LeadRoutingService(new Random());
        Agent only = agent("only", milano);

        var result = service.pickAgent(lead, List.of(new AgentWithLoad(only, 4)));

        assertThat(result.map(AgentWithLoad::agent)).contains(only);
    }

    @Test
    // Tied agents: asserts one of the two is returned without pinning which, so the test survives any seed.
    void tiedAgentsAreChoosenByRandom() {
        var service = new LeadRoutingService(new Random(0));
        Agent a = agent("a", milano);
        Agent b = agent("b", milano);

        var result = service.pickAgent(lead, List.of(
                new AgentWithLoad(a, 2),
                new AgentWithLoad(b, 2)));

        assertThat(result).isPresent();
        assertThat(List.of(a, b)).contains(result.get().agent());
    }

    private Agent agent(String suffix, City city) {
        return new Agent(new AgentId(UUID.randomUUID()), city, true);
    }
}
