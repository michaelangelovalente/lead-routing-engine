package com.casavo.leadrouting.leadrouting.domain;

import com.casavo.leadrouting.leadrouting.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Lead has exactly two states: NEW → ASSIGNED. markAssigned() is the only transition.
class LeadTest {

    private Lead freshLead() {
        return new Lead(
                LeadId.generate(),
                new CustomerContact("Mario Rossi", "mario@example.com", "+39333123"),
                "PROP-001",
                new City("Milano"),
                Instant.now(),
                new IdempotencyKey("key-1"));
    }

    @Test
    // Initial state is part of the contract — downstream branching on NEW depends on this.
    void newLead_hasStatusNew() {
        assertThat(freshLead().status()).isEqualTo(LeadStatus.NEW);
    }

    @Test
    // Happy path — verify the transition works before testing what happens when it is violated.
    void markAssigned_transitionsToAssigned() {
        Lead lead = freshLead();
        lead.markAssigned();
        assertThat(lead.status()).isEqualTo(LeadStatus.ASSIGNED);
    }

    @Test
    // Re-assigning is a domain violation; message is asserted because it surfaces in logs and error responses.
    void markAssigned_twice_throwsIllegalState() {
        Lead lead = freshLead();
        lead.markAssigned();
        assertThatThrownBy(lead::markAssigned)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ASSIGNED");
    }

    @Test
    // A blank propertyReference makes the lead uninterpretable — reject at construction, not at runtime.
    void blankPropertyReference_throwsIllegalArgument() {
        assertThatThrownBy(() -> new Lead(
                LeadId.generate(),
                new CustomerContact("Mario", "m@e.com", "+39333"),
                "   ",
                new City("Milano"),
                Instant.now(),
                new IdempotencyKey("key-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
