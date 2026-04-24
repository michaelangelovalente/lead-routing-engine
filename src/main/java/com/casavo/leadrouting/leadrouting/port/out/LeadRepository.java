package com.casavo.leadrouting.leadrouting.port.out;

import com.casavo.leadrouting.leadrouting.domain.model.IdempotencyKey;
import com.casavo.leadrouting.leadrouting.domain.model.Lead;

import java.util.Optional;

public interface LeadRepository {
    Optional<Lead> findByIdempotencyKey(IdempotencyKey key);
    void save(Lead lead);
}
