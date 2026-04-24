package com.casavo.leadrouting.leadrouting.adapter.in.rest;

import com.casavo.leadrouting.leadrouting.adapter.in.rest.dto.AssignmentPageResponse;
import com.casavo.leadrouting.leadrouting.application.query.Cursor;
import com.casavo.leadrouting.leadrouting.application.query.AssignmentPage;
import com.casavo.leadrouting.leadrouting.application.query.ListAssignmentsUseCase;
import com.casavo.leadrouting.leadrouting.domain.model.AgentId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/agents")
public class AgentAssignmentsController {

    private final ListAssignmentsUseCase listAssignmentsUseCase;

    public AgentAssignmentsController(ListAssignmentsUseCase listAssignmentsUseCase) {
        this.listAssignmentsUseCase = listAssignmentsUseCase;
    }

    @GetMapping("/{agentId}/assignments")
    public ResponseEntity<AssignmentPageResponse> listAssignments(
            @PathVariable UUID agentId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {

        int clampedLimit = Math.clamp(limit, 1, 100);
        Optional<Cursor> decodedCursor = Optional.ofNullable(cursor).map(Cursor::decode);

        AssignmentPage page = listAssignmentsUseCase.listByAgent(
                new AgentId(agentId), decodedCursor, clampedLimit);

        return ResponseEntity.ok(AssignmentPageResponse.from(page));
    }
}
