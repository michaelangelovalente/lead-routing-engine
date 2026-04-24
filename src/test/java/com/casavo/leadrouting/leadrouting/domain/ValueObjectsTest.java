package com.casavo.leadrouting.leadrouting.domain;

import com.casavo.leadrouting.leadrouting.domain.model.City;
import com.casavo.leadrouting.leadrouting.domain.model.CustomerContact;
import com.casavo.leadrouting.leadrouting.domain.model.IdempotencyKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Value objects validate and normalise at construction — illegal state cannot be represented.
class ValueObjectsTest {

    @Test
    // Portals send city names in arbitrary casing; normalising here means routing comparisons always work.
    void city_normalizesToLowercase() {
        assertThat(new City("Milano").name()).isEqualTo("milano");
        assertThat(new City("  ROMA  ").name()).isEqualTo("roma");
    }

    @Test
    // A blank city makes the lead unroutable — reject immediately rather than produce zero candidates silently.
    void city_rejectsBlank() {
        assertThatThrownBy(() -> new City(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new City("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    // DB column is VARCHAR(128) — reject here to get a clear domain error instead of a JDBC truncation exception.
    void city_rejectsTooLong() {
        assertThatThrownBy(() -> new City("a".repeat(129)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    // Email addresses are case-insensitive (RFC 5321) — normalising prevents the same customer appearing twice.
    void customerContact_normalizesEmailToLowercase() {
        var contact = new CustomerContact("Mario", "MARIO@EXAMPLE.COM", "+39333");
        assertThat(contact.email()).isEqualTo("mario@example.com");
    }

    @Test
    // Web forms often add whitespace — trimming here keeps the name clean in notifications and the audit trail.
    void customerContact_trimsName() {
        var contact = new CustomerContact("  Mario Rossi  ", "m@e.com", "+39333");
        assertThat(contact.name()).isEqualTo("Mario Rossi");
    }

    @Test
    // An invalid email means the notification relay cannot deliver — reject at the domain boundary, not at delivery.
    void customerContact_rejectsInvalidEmail() {
        assertThatThrownBy(() -> new CustomerContact("Mario", "not-an-email", "+39333"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    // A blank key would match every other blank key in the DB — it cannot fulfill the idempotency contract.
    void idempotencyKey_rejectsBlank() {
        assertThatThrownBy(() -> new IdempotencyKey(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    // DB column is VARCHAR(255) — reject here to surface a clear error instead of a JDBC truncation exception.
    void idempotencyKey_rejectsTooLong() {
        assertThatThrownBy(() -> new IdempotencyKey("a".repeat(256)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
