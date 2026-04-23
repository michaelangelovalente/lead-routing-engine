package com.casavo.leadrouting.leadrouting.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class Lead {

    private final LeadId id;
    private final CustomerContact customer;
    private final String propertyReference;
    private final City city;
    private final Instant receivedAt;
    private final IdempotencyKey idempotencyKey;
    private LeadStatus status;

    public Lead(
            LeadId id,
            CustomerContact customer,
            String propertyReference,
            City city,
            Instant receivedAt,
            IdempotencyKey idempotencyKey) {
        this.id = Objects.requireNonNull(id);
        this.customer = Objects.requireNonNull(customer);
        this.propertyReference = validatePropertyReference(propertyReference);
        this.city = Objects.requireNonNull(city);
        this.receivedAt = Objects.requireNonNull(receivedAt);
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
        this.status = LeadStatus.NEW;
    }

    public static Lead reconstitute(
            LeadId id,
            CustomerContact customer,
            String propertyReference,
            City city,
            Instant receivedAt,
            IdempotencyKey idempotencyKey,
            LeadStatus status) {
        var lead = new Lead(id, customer, propertyReference, city, receivedAt, idempotencyKey);
        lead.status = status;
        return lead;
    }

    public void markAssigned() {
        if (status != LeadStatus.NEW) {
            throw new IllegalStateException(
                    "Lead " + id + " cannot transition from " + status + " to ASSIGNED");
        }
        this.status = LeadStatus.ASSIGNED;
    }

    private static String validatePropertyReference(String ref) {
        Objects.requireNonNull(ref);
        String trimmed = ref.trim();
        if (trimmed.isBlank() || trimmed.length() > 64) {
            throw new IllegalArgumentException("Invalid property reference");
        }
        return trimmed;
    }

    public LeadId id() { return id; }
    public CustomerContact customer() { return customer; }
    public String propertyReference() { return propertyReference; }
    public City city() { return city; }
    public Instant receivedAt() { return receivedAt; }
    public IdempotencyKey idempotencyKey() { return idempotencyKey; }
    public LeadStatus status() { return status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lead lead)) return false;
        return Objects.equals(id, lead.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
