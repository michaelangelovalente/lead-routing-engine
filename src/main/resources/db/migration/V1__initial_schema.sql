CREATE TABLE agents (
    id          UUID PRIMARY KEY,
    city        VARCHAR(128) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_agents_city_active
    ON agents (city) WHERE active = true;

CREATE TABLE leads (
    id                 UUID PRIMARY KEY,
    idempotency_key    VARCHAR(255) NOT NULL,
    customer_name      VARCHAR(255) NOT NULL,
    customer_email     VARCHAR(320) NOT NULL,
    customer_phone     VARCHAR(20)  NOT NULL,
    property_reference VARCHAR(64)  NOT NULL,
    city               VARCHAR(128) NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    received_at        TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_leads_idempotency_key UNIQUE (idempotency_key)
);

CREATE TABLE assignments (
    id           UUID PRIMARY KEY,
    lead_id      UUID NOT NULL,
    agent_id     UUID NOT NULL,
    assigned_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_assignments_lead  FOREIGN KEY (lead_id)  REFERENCES leads(id),
    CONSTRAINT fk_assignments_agent FOREIGN KEY (agent_id) REFERENCES agents(id),
    CONSTRAINT uk_assignments_lead  UNIQUE (lead_id)
);

CREATE INDEX idx_assignments_agent_timeline
    ON assignments (agent_id, assigned_at DESC, id DESC);

CREATE INDEX idx_assignments_lead
    ON assignments (lead_id);

CREATE TABLE outbox_event (
    id              UUID PRIMARY KEY,
    event_type      VARCHAR(64)  NOT NULL,
    payload         JSONB        NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ,
    retry_count     INTEGER      NOT NULL DEFAULT 0,
    failed_at       TIMESTAMPTZ,
    last_error      TEXT
);

CREATE INDEX idx_outbox_unpublished
    ON outbox_event (created_at)
    WHERE published_at IS NULL AND failed_at IS NULL;

INSERT INTO agents (id, city, active) VALUES
    ('11111111-1111-1111-1111-111111111111', 'milano', true),
    ('22222222-2222-2222-2222-222222222222', 'milano', true),
    ('33333333-3333-3333-3333-333333333333', 'roma',   true),
    ('44444444-4444-4444-4444-444444444444', 'torino', true);
