package com.casavo.leadrouting.leadrouting.domain;

import com.casavo.leadrouting.leadrouting.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// All three eligibility conditions in Agent.tryAssign() are independent gates — each test isolates one.
class AgentTest {

    private final City milano = new City("Milano");
    private final City roma = new City("Roma");
    private final Instant now = Instant.now();

    private Lead milanLead() {
        return new Lead(
                LeadId.generate(),
                new CustomerContact("Mario Rossi", "mario@example.com", "+39333123"),
                "PROP-001",
                milano,
                now,
                new IdempotencyKey("key-1"));
    }

    @Test
    // Baseline: valid call produces a valid Assignment linking back to the correct agent.
    void happyPath_assignsLead() {
        Agent agent = new Agent(new AgentId(UUID.randomUUID()), milano, true);
        Optional<Assignment> result = agent.tryAssign(milanLead(), 0, now);
        assertThat(result).isPresent();
        assertThat(result.get().agentId()).isEqualTo(agent.id());
    }

    @Test
    // active=false is a hard gate — city and load being fine does not override it.
    void inactiveAgent_returnsEmpty() {
        Agent inactive = new Agent(new AgentId(UUID.randomUUID()), milano, false);
        assertThat(inactive.tryAssign(milanLead(), 0, now)).isEmpty();
    }

    @Test
    // City match is enforced inside the domain, not only at the query level — second line of defence.
    void cityMismatch_returnsEmpty() {
        Agent romaAgent = new Agent(new AgentId(UUID.randomUUID()), roma, true);
        assertThat(romaAgent.tryAssign(milanLead(), 0, now)).isEmpty();
    }

    @Test
    // Load == MAX is rejected (>=); off-by-one here would cause silent over-assignment.
    void atCapacity_returnsEmpty() {
        Agent agent = new Agent(new AgentId(UUID.randomUUID()), milano, true);
        assertThat(agent.tryAssign(milanLead(), Agent.MAX_LEADS_PER_WINDOW, now)).isEmpty();
    }

    @Test
    // Load == MAX-1 is accepted — completes the boundary pair with atCapacity_returnsEmpty.
    void oneUnderCapacity_assigns() {
        Agent agent = new Agent(new AgentId(UUID.randomUUID()), milano, true);
        assertThat(agent.tryAssign(milanLead(), Agent.MAX_LEADS_PER_WINDOW - 1, now)).isPresent();
    }

    @Test
    // Negative load means a corrupt count query upstream — fail fast rather than treat as under-capacity.
    void negativeLoad_throwsIllegalArgument() {
        Agent agent = new Agent(new AgentId(UUID.randomUUID()), milano, true);
        assertThatThrownBy(() -> agent.tryAssign(milanLead(), -1, now))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
