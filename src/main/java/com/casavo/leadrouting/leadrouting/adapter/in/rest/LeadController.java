package com.casavo.leadrouting.leadrouting.adapter.in.rest;

import com.casavo.leadrouting.common.NoEligibleAgentException;
import com.casavo.leadrouting.leadrouting.adapter.in.rest.dto.AssignmentResponse;
import com.casavo.leadrouting.leadrouting.adapter.in.rest.dto.CreateLeadRequest;
import com.casavo.leadrouting.leadrouting.adapter.in.rest.dto.LeadResponse;
import com.casavo.leadrouting.leadrouting.application.command.AssignLeadCommand;
import com.casavo.leadrouting.leadrouting.application.command.AssignLeadUseCase;
import com.casavo.leadrouting.leadrouting.application.command.AssignmentResult;
import com.casavo.leadrouting.leadrouting.domain.model.City;
import com.casavo.leadrouting.leadrouting.domain.model.CustomerContact;
import com.casavo.leadrouting.leadrouting.domain.model.IdempotencyKey;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/leads")
public class LeadController {

    private final AssignLeadUseCase assignLeadUseCase;

    public LeadController(AssignLeadUseCase assignLeadUseCase) {
        this.assignLeadUseCase = assignLeadUseCase;
    }

    @PostMapping
    public ResponseEntity<LeadResponse> createLead(
            @RequestHeader("Idempotency-Key") String idempotencyKeyHeader,
            @Valid @RequestBody CreateLeadRequest request) {

        var cmd = new AssignLeadCommand(
                new CustomerContact(
                        request.customer().name(),
                        request.customer().email(),
                        request.customer().phone()),
                request.propertyReference(),
                new City(request.city()),
                new IdempotencyKey(idempotencyKeyHeader));

        AssignmentResult result = assignLeadUseCase.assign(cmd);

        return switch (result) {
            case AssignmentResult.Assigned r -> {
                URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(r.lead().id().value())
                        .toUri();
                yield ResponseEntity.created(location)
                        .body(LeadResponse.from(r.lead(), r.assignment()));
            }
            case AssignmentResult.AlreadyAssigned r ->
                    ResponseEntity.ok(LeadResponse.from(r.lead(), r.assignment()));
            case AssignmentResult.NoEligibleAgent r ->
                    throw new NoEligibleAgentException(r.leadId(), r.city());
        };
    }

}
