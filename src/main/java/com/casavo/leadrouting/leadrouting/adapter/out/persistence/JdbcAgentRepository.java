package com.casavo.leadrouting.leadrouting.adapter.out.persistence;

import com.casavo.leadrouting.leadrouting.domain.model.Agent;
import com.casavo.leadrouting.leadrouting.domain.model.AgentId;
import com.casavo.leadrouting.leadrouting.domain.model.City;
import com.casavo.leadrouting.leadrouting.port.out.AgentRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcAgentRepository implements AgentRepository {

    private final JdbcClient jdbc;

    public JdbcAgentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsById(AgentId id) {
        Integer count = jdbc.sql("SELECT COUNT(*) FROM agents WHERE id = :id")
                .param("id", id.value())
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    @Override
    public List<Agent> findActiveInCityForUpdate(City city) {
        return jdbc.sql("""
                SELECT id, city, active
                FROM agents
                WHERE city = :city AND active = true
                FOR UPDATE
                """)
                .param("city", city.name())
                .query(this::mapRow)
                .list();
    }

    private Agent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Agent(
                new AgentId(rs.getObject("id", UUID.class)),
                new City(rs.getString("city")),
                rs.getBoolean("active"));
    }
}
