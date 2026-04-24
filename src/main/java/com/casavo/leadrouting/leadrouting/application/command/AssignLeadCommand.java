package com.casavo.leadrouting.leadrouting.application.command;

import com.casavo.leadrouting.leadrouting.domain.model.City;
import com.casavo.leadrouting.leadrouting.domain.model.CustomerContact;
import com.casavo.leadrouting.leadrouting.domain.model.IdempotencyKey;

public record AssignLeadCommand(
        CustomerContact customer,
        String propertyReference,
        City city,
        IdempotencyKey idempotencyKey
) {}
