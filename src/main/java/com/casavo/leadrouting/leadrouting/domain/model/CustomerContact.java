package com.casavo.leadrouting.leadrouting.domain.model;

import java.util.Locale;
import java.util.Objects;

public record CustomerContact(String name, String email, String phone) {
    public CustomerContact {
        Objects.requireNonNull(name);
        Objects.requireNonNull(email);
        Objects.requireNonNull(phone);

        name = name.trim();
        if (name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException("Invalid customer name");
        }

        email = email.trim().toLowerCase(Locale.ROOT);
        if (email.isBlank() || email.length() > 320 || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        phone = phone.replaceAll("\\s", "");
        if (phone.isBlank() || phone.length() > 20) {
            throw new IllegalArgumentException("Invalid phone number");
        }
    }
}
