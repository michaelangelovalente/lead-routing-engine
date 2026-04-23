package com.casavo.leadrouting.leadrouting.domain.model;

import java.util.Objects;
import java.util.UUID;

public record AgentId(UUID value) {
    public AgentId {
        Objects.requireNonNull(value);
    }
}
