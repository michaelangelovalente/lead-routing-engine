package com.casavo.leadrouting.leadrouting.application.command;

import com.casavo.leadrouting.leadrouting.domain.event.LeadAssignedEvent;
import com.casavo.leadrouting.leadrouting.domain.model.Agent;
import com.casavo.leadrouting.leadrouting.domain.model.AgentId;
import com.casavo.leadrouting.leadrouting.domain.model.Assignment;
import com.casavo.leadrouting.leadrouting.domain.model.Lead;
import com.casavo.leadrouting.leadrouting.domain.model.LeadId;
import com.casavo.leadrouting.leadrouting.domain.model.LeadStatus;
import com.casavo.leadrouting.leadrouting.domain.service.AgentWithLoad;
import com.casavo.leadrouting.leadrouting.domain.service.LeadRoutingService;
import com.casavo.leadrouting.leadrouting.port.out.AgentRepository;
import com.casavo.leadrouting.leadrouting.port.out.AssignmentRepository;
import com.casavo.leadrouting.leadrouting.port.out.LeadRepository;
import com.casavo.leadrouting.leadrouting.port.out.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@Service
public class AssignLeadUseCaseImpl implements AssignLeadUseCase {

    private final LeadRepository leadRepo;
    private final AgentRepository agentRepo;
    private final AssignmentRepository assignmentRepo;
    private final OutboxRepository outboxRepo;
    private final LeadRoutingService routingService;
    private final Clock clock;

    public AssignLeadUseCaseImpl(
            LeadRepository leadRepo,
            AgentRepository agentRepo,
            AssignmentRepository assignmentRepo,
            OutboxRepository outboxRepo,
            LeadRoutingService routingService,
            Clock clock) {
        this.leadRepo = leadRepo;
        this.agentRepo = agentRepo;
        this.assignmentRepo = assignmentRepo;
        this.outboxRepo = outboxRepo;
        this.routingService = routingService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AssignmentResult assign(AssignLeadCommand cmd) {
        Instant now = clock.instant();

        // 1. Idempotency check
        Optional<Lead> existing = leadRepo.findByIdempotencyKey(cmd.idempotencyKey());
        if (existing.isPresent()) {
            Lead lead = existing.get();
            if (lead.status() == LeadStatus.ASSIGNED) {
                Assignment existingAssignment = assignmentRepo.findByLeadId(lead.id())
                        .orElseThrow(() -> new IllegalStateException(
                                "Assigned lead without assignment: " + lead.id()));
                return new AssignmentResult.AlreadyAssigned(lead, existingAssignment);
            }
            return new AssignmentResult.NoEligibleAgent(lead.id(), lead.city());
        }

        // 2. Construct Lead
        Lead lead = new Lead(
                LeadId.generate(), cmd.customer(), cmd.propertyReference(),
                cmd.city(), now, cmd.idempotencyKey());

        // 3. Lock agents in city with SELECT FOR UPDATE
        List<Agent> activeAgents = agentRepo.findActiveInCityForUpdate(cmd.city());

        // 4. Count current loads per agent (window = last 24h) in one query, filter under-capacity
        Instant windowStart = now.minus(Agent.ASSIGNMENT_WINDOW);
        List<AgentId> agentIds = activeAgents.stream().map(Agent::id).toList();
        Map<AgentId, Integer> loadByAgent = agentIds.isEmpty()
                ? Map.of()
                : assignmentRepo.countAssignmentsSincePerAgent(agentIds, windowStart);
        List<AgentWithLoad> candidates = activeAgents.stream()
                .map(a -> new AgentWithLoad(a, loadByAgent.getOrDefault(a.id(), 0)))
                .filter(awl -> awl.currentLoad() < Agent.MAX_LEADS_PER_WINDOW)
                .toList();

        // 5. Route: service returns the selected AgentWithLoad directly
        Optional<AgentWithLoad> chosen = routingService.pickAgent(lead, candidates);

        if (chosen.isEmpty()) {
            leadRepo.save(lead);
            return new AssignmentResult.NoEligibleAgent(lead.id(), lead.city());
        }

        // 6. Aggregate produces Assignment
        AgentWithLoad selected = chosen.get();
        Agent agent = selected.agent();

        Assignment assignment = agent.tryAssign(lead, selected.currentLoad(), now)
                .orElseThrow(() -> new IllegalStateException(
                        "Routing picked ineligible agent: " + agent.id()));

        // 7. Transition lead status
        lead.markAssigned();

        // 8. Persist: three writes in same transaction
        leadRepo.save(lead);
        assignmentRepo.save(assignment);
        outboxRepo.append(buildEvent(lead, agent, assignment));

        return new AssignmentResult.Assigned(lead, assignment);
    }

    private LeadAssignedEvent buildEvent(Lead lead, Agent agent, Assignment assignment) {
        return new LeadAssignedEvent(
                UUID.randomUUID().toString(),
                lead.id().value().toString(),
                agent.id().value().toString(),
                assignment.id().value().toString(),
                lead.city().name(),
                assignment.assignedAt().toString(),
                1);
    }
}
