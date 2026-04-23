package com.casavo.leadrouting.leadrouting.domain.model;

import java.util.Objects;
import java.util.UUID;

public record AssignmentId(UUID value) {
    public AssignmentId {
        Objects.requireNonNull(value);
    }

    public static AssignmentId generate() {
        return new AssignmentId(UUID.randomUUID());
    }
}
