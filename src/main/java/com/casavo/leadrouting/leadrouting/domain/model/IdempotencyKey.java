package com.casavo.leadrouting.leadrouting.domain.model;

import java.util.Objects;

public record IdempotencyKey(String value) {
    public IdempotencyKey {
        Objects.requireNonNull(value);
        if (value.isBlank() || value.length() > 255) {
            throw new IllegalArgumentException("Idempotency key must be 1–255 characters");
        }
    }
}
