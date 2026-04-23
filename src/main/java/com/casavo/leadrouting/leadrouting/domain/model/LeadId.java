package com.casavo.leadrouting.leadrouting.domain.model;

import java.util.Objects;
import java.util.UUID;

public record LeadId(UUID value) {
    public LeadId {
        Objects.requireNonNull(value);
    }

    public static LeadId generate() {
        return new LeadId(UUID.randomUUID());
    }
}
