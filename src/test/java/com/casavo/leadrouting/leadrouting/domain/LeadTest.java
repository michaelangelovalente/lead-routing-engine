package com.casavo.leadrouting.leadrouting.domain;

import com.casavo.leadrouting.leadrouting.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void newLead_hasStatusNew() {
        assertThat(freshLead().status()).isEqualTo(LeadStatus.NEW);
    }

    @Test
    void markAssigned_transitionsToAssigned() {
        Lead lead = freshLead();
        lead.markAssigned();
        assertThat(lead.status()).isEqualTo(LeadStatus.ASSIGNED);
    }

    @Test
    void markAssigned_twice_throwsIllegalState() {
        Lead lead = freshLead();
        lead.markAssigned();
        assertThatThrownBy(lead::markAssigned)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ASSIGNED");
    }

    @Test
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
