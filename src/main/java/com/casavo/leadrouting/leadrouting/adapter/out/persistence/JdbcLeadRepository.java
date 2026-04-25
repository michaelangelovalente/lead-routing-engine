package com.casavo.leadrouting.leadrouting.adapter.out.persistence;

import com.casavo.leadrouting.leadrouting.domain.model.City;
import com.casavo.leadrouting.leadrouting.domain.model.CustomerContact;
import com.casavo.leadrouting.leadrouting.domain.model.IdempotencyKey;
import com.casavo.leadrouting.leadrouting.domain.model.Lead;
import com.casavo.leadrouting.leadrouting.domain.model.LeadId;
import com.casavo.leadrouting.leadrouting.domain.model.LeadStatus;
import com.casavo.leadrouting.leadrouting.port.out.LeadRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcLeadRepository implements LeadRepository {

    private final JdbcClient jdbc;

    public JdbcLeadRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Lead> findByIdempotencyKey(IdempotencyKey key) {
        return jdbc.sql("""
                SELECT id, idempotency_key, customer_name, customer_email, customer_phone,
                       property_reference, city, status, received_at
                FROM leads WHERE idempotency_key = :key
                """)
                .param("key", key.value())
                .query(this::mapRow)
                .optional();
    }

    @Override
    public void save(Lead lead) {
        jdbc.sql("""
                INSERT INTO leads
                    (id, idempotency_key, customer_name, customer_email, customer_phone,
                     property_reference, city, status, received_at)
                VALUES
                    (:id, :idempotencyKey, :name, :email, :phone,
                     :propertyReference, :city, :status, :receivedAt)
                ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status
                """)
                .param("id", lead.id().value())
                .param("idempotencyKey", lead.idempotencyKey().value())
                .param("name", lead.customer().name())
                .param("email", lead.customer().email())
                .param("phone", lead.customer().phone())
                .param("propertyReference", lead.propertyReference())
                .param("city", lead.city().name())
                .param("status", lead.status().name())
                .param("receivedAt", toOdt(lead.receivedAt()))
                .update();
    }

    private static OffsetDateTime toOdt(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private Lead mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Lead.reconstitute(
                new LeadId(rs.getObject("id", UUID.class)),
                new CustomerContact(
                        rs.getString("customer_name"),
                        rs.getString("customer_email"),
                        rs.getString("customer_phone")),
                rs.getString("property_reference"),
                new City(rs.getString("city")),
                rs.getTimestamp("received_at").toInstant(),
                new IdempotencyKey(rs.getString("idempotency_key")),
                LeadStatus.valueOf(rs.getString("status")));
    }
}
