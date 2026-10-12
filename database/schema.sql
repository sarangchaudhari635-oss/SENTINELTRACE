-- Reference PostgreSQL schema for SentinelTrace.
-- Use either this schema or Hibernate-generated schema as the source of truth.
-- If using this file manually, align JPA mappings with the table and column names.

CREATE TABLE IF NOT EXISTS detection_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_code VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    threshold INTEGER NOT NULL CHECK (threshold > 0),
    window_seconds INTEGER NOT NULL CHECK (window_seconds > 0),
    severity VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS security_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    source_ip VARCHAR(45) NOT NULL,
    username VARCHAR(100),
    endpoint VARCHAR(255),
    details TEXT,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_events_type_ip_time
    ON security_events (event_type, source_ip, occurred_at);
CREATE INDEX IF NOT EXISTS idx_events_time
    ON security_events (occurred_at);

CREATE TABLE IF NOT EXISTS incidents (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL REFERENCES detection_rules(id),
    fingerprint VARCHAR(180) NOT NULL,
    source_ip VARCHAR(45) NOT NULL,
    title VARCHAR(200) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    risk_score INTEGER NOT NULL CHECK (risk_score BETWEEN 0 AND 100),
    status VARCHAR(20) NOT NULL,
    first_seen TIMESTAMPTZ NOT NULL,
    last_seen TIMESTAMPTZ NOT NULL,
    evidence_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_incidents_status_severity_time
    ON incidents (status, severity, last_seen);
CREATE INDEX IF NOT EXISTS idx_incidents_fingerprint_status
    ON incidents (fingerprint, status);

CREATE TABLE IF NOT EXISTS incident_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents(id),
    action VARCHAR(40) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
