package com.casavo.leadrouting.leadrouting.application.query;

import com.casavo.leadrouting.common.AgentNotFoundException;
import com.casavo.leadrouting.leadrouting.domain.model.AgentId;
import com.casavo.leadrouting.leadrouting.domain.model.Assignment;
import com.casavo.leadrouting.leadrouting.port.out.AgentRepository;
import com.casavo.leadrouting.leadrouting.port.out.AssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ListAssignmentsUseCaseImpl implements ListAssignmentsUseCase {

    private final AgentRepository agentRepo;
    private final AssignmentRepository assignmentRepo;

    public ListAssignmentsUseCaseImpl(AgentRepository agentRepo, AssignmentRepository assignmentRepo) {
        this.agentRepo = agentRepo;
        this.assignmentRepo = assignmentRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentPage listByAgent(AgentId agentId, Optional<Cursor> cursor, int limit) {
        if (!agentRepo.existsById(agentId)) {
            throw new AgentNotFoundException(agentId);
        }

        Optional<Instant> cursorAt = cursor.map(Cursor::assignedAt);
        Optional<UUID> cursorId = cursor.map(Cursor::id);

        List<Assignment> rows = assignmentRepo.findByAgentId(agentId, cursorAt, cursorId, limit + 1);

        boolean hasMore = rows.size() > limit;
        List<Assignment> data = hasMore ? rows.subList(0, limit) : rows;

        Optional<Cursor> nextCursor = hasMore
                ? Optional.of(new Cursor(
                        data.get(data.size() - 1).assignedAt(),
                        data.get(data.size() - 1).id().value()))
                : Optional.empty();

        return new AssignmentPage(data, nextCursor, hasMore, limit);
    }
}
