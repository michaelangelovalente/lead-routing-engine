package com.casavo.leadrouting.leadrouting.adapter.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLeadRequest(
        @NotNull @Valid CustomerContactDto customer,
        @NotBlank @Size(max = 64) String propertyReference,
        @NotBlank @Size(max = 128) String city
) {}
