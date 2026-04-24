package com.casavo.leadrouting.leadrouting.domain;

import com.casavo.leadrouting.leadrouting.domain.model.City;
import com.casavo.leadrouting.leadrouting.domain.model.CustomerContact;
import com.casavo.leadrouting.leadrouting.domain.model.IdempotencyKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectsTest {

    @Test
    void city_normalizesToLowercase() {
        assertThat(new City("Milano").name()).isEqualTo("milano");
        assertThat(new City("  ROMA  ").name()).isEqualTo("roma");
    }

    @Test
    void city_rejectsBlank() {
        assertThatThrownBy(() -> new City(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new City("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void city_rejectsTooLong() {
        assertThatThrownBy(() -> new City("a".repeat(129)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void customerContact_normalizesEmailToLowercase() {
        var contact = new CustomerContact("Mario", "MARIO@EXAMPLE.COM", "+39333");
        assertThat(contact.email()).isEqualTo("mario@example.com");
    }

    @Test
    void customerContact_trimsName() {
        var contact = new CustomerContact("  Mario Rossi  ", "m@e.com", "+39333");
        assertThat(contact.name()).isEqualTo("Mario Rossi");
    }

    @Test
    void customerContact_rejectsInvalidEmail() {
        assertThatThrownBy(() -> new CustomerContact("Mario", "not-an-email", "+39333"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void idempotencyKey_rejectsBlank() {
        assertThatThrownBy(() -> new IdempotencyKey(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void idempotencyKey_rejectsTooLong() {
        assertThatThrownBy(() -> new IdempotencyKey("a".repeat(256)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
