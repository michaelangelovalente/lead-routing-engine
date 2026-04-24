package com.casavo.leadrouting.leadrouting.adapter.out.persistence;

import com.casavo.leadrouting.leadrouting.domain.model.AgentId;
import com.casavo.leadrouting.leadrouting.domain.model.Assignment;
import com.casavo.leadrouting.leadrouting.domain.model.AssignmentId;
import com.casavo.leadrouting.leadrouting.domain.model.LeadId;
import com.casavo.leadrouting.leadrouting.port.out.AssignmentRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JdbcAssignmentRepository implements AssignmentRepository {

    private final JdbcClient jdbc;

    public JdbcAssignmentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Assignment assignment) {
        jdbc.sql("""
                INSERT INTO assignments (id, lead_id, agent_id, assigned_at)
                VALUES (:id, :leadId, :agentId, :assignedAt)
                """)
                .param("id", assignment.id().value())
                .param("leadId", assignment.leadId().value())
                .param("agentId", assignment.agentId().value())
                .param("assignedAt", assignment.assignedAt())
                .update();
    }

    @Override
    public Map<AgentId, Integer> countAssignmentsSincePerAgent(List<AgentId> agentIds, Instant since) {
        List<UUID> ids = agentIds.stream().map(AgentId::value).toList();
        return jdbc.sql("""
                SELECT agent_id, COUNT(*) AS cnt
                FROM assignments
                WHERE agent_id IN (:agentIds)
                  AND assigned_at > :since
                GROUP BY agent_id
                """)
                .param("agentIds", ids)
                .param("since", since)
                .query(this::mapLoadRow)
                .list()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map.Entry<AgentId, Integer> mapLoadRow(ResultSet rs, int rowNum) throws SQLException {
        return Map.entry(
                new AgentId(rs.getObject("agent_id", UUID.class)),
                rs.getInt("cnt"));
    }

    @Override
    public Optional<Assignment> findByLeadId(LeadId leadId) {
        return jdbc.sql("""
                SELECT id, lead_id, agent_id, assigned_at
                FROM assignments WHERE lead_id = :leadId
                """)
                .param("leadId", leadId.value())
                .query(this::mapRow)
                .optional();
    }

    @Override
    public List<Assignment> findByAgentId(AgentId agentId, Optional<Instant> cursorAt, Optional<UUID> cursorId, int maxResults) {
        if (cursorAt.isPresent() && cursorId.isPresent()) {
            return jdbc.sql("""
                    SELECT id, lead_id, agent_id, assigned_at
                    FROM assignments
                    WHERE agent_id = :agentId
                      AND (assigned_at, id) < (:cursorAt, :cursorId)
                    ORDER BY assigned_at DESC, id DESC
                    LIMIT :limit
                    """)
                    .param("agentId", agentId.value())
                    .param("cursorAt", cursorAt.get())
                    .param("cursorId", cursorId.get())
                    .param("limit", maxResults)
                    .query(this::mapRow)
                    .list();
        }
        return jdbc.sql("""
                SELECT id, lead_id, agent_id, assigned_at
                FROM assignments
                WHERE agent_id = :agentId
                ORDER BY assigned_at DESC, id DESC
                LIMIT :limit
                """)
                .param("agentId", agentId.value())
                .param("limit", maxResults)
                .query(this::mapRow)
                .list();
    }

    private Assignment mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Assignment(
                new AssignmentId(rs.getObject("id", UUID.class)),
                new LeadId(rs.getObject("lead_id", UUID.class)),
                new AgentId(rs.getObject("agent_id", UUID.class)),
                rs.getTimestamp("assigned_at").toInstant());
    }
}
