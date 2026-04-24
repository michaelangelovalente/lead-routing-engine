package com.casavo.leadrouting.leadrouting.domain;

import com.casavo.leadrouting.leadrouting.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void happyPath_assignsLead() {
        Agent agent = new Agent(new AgentId(UUID.randomUUID()), milano, true);
        Optional<Assignment> result = agent.tryAssign(milanLead(), 0, now);
        assertThat(result).isPresent();
        assertThat(result.get().agentId()).isEqualTo(agent.id());
    }

    @Test
    void inactiveAgent_returnsEmpty() {
        Agent inactive = new Agent(new AgentId(UUID.randomUUID()), milano, false);
        assertThat(inactive.tryAssign(milanLead(), 0, now)).isEmpty();
    }

    @Test
    void cityMismatch_returnsEmpty() {
        Agent romaAgent = new Agent(new AgentId(UUID.randomUUID()), roma, true);
        assertThat(romaAgent.tryAssign(milanLead(), 0, now)).isEmpty();
    }

    @Test
    void atCapacity_returnsEmpty() {
        Agent agent = new Agent(new AgentId(UUID.randomUUID()), milano, true);
        assertThat(agent.tryAssign(milanLead(), Agent.MAX_LEADS_PER_WINDOW, now)).isEmpty();
    }

    @Test
    void oneUnderCapacity_assigns() {
        Agent agent = new Agent(new AgentId(UUID.randomUUID()), milano, true);
        assertThat(agent.tryAssign(milanLead(), Agent.MAX_LEADS_PER_WINDOW - 1, now)).isPresent();
    }

    @Test
    void negativeLoad_throwsIllegalArgument() {
        Agent agent = new Agent(new AgentId(UUID.randomUUID()), milano, true);
        assertThatThrownBy(() -> agent.tryAssign(milanLead(), -1, now))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
