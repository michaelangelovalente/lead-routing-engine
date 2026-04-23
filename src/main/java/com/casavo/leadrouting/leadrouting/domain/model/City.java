package com.casavo.leadrouting.leadrouting.domain.model;

import java.util.Locale;
import java.util.Objects;

public record City(String name) {
    public City {
        Objects.requireNonNull(name);
        name = name.trim().toLowerCase(Locale.ITALIAN);
        if (name.isBlank() || name.length() > 128) {
            throw new IllegalArgumentException("City cannot be blank or exceed 128 characters");
        }
    }
}
