# Database Schema

The MVP uses four tables.

## 1. security_events
- `id BIGSERIAL PRIMARY KEY`
- `event_type VARCHAR(40) NOT NULL`
- `source_ip VARCHAR(45) NOT NULL`
- `username VARCHAR(100)`
- `endpoint VARCHAR(255)`
- `details TEXT`
- `occurred_at TIMESTAMPTZ NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

Indexes:
- `(event_type, source_ip, occurred_at)`
- `(occurred_at)`

## 2. detection_rules
- `id BIGSERIAL PRIMARY KEY`
- `rule_code VARCHAR(60) NOT NULL UNIQUE`
- `name VARCHAR(120) NOT NULL`
- `event_type VARCHAR(40) NOT NULL`
- `threshold INTEGER NOT NULL CHECK (threshold > 0)`
- `window_seconds INTEGER NOT NULL CHECK (window_seconds > 0)`
- `severity VARCHAR(20) NOT NULL`
- `enabled BOOLEAN NOT NULL DEFAULT TRUE`

## 3. incidents
- `id BIGSERIAL PRIMARY KEY`
- `rule_id BIGINT NOT NULL REFERENCES detection_rules(id)`
- `fingerprint VARCHAR(180) NOT NULL`
- `source_ip VARCHAR(45) NOT NULL`
- `title VARCHAR(200) NOT NULL`
- `severity VARCHAR(20) NOT NULL`
- `risk_score INTEGER NOT NULL CHECK (risk_score BETWEEN 0 AND 100)`
- `status VARCHAR(20) NOT NULL`
- `first_seen TIMESTAMPTZ NOT NULL`
- `last_seen TIMESTAMPTZ NOT NULL`
- `evidence_count INTEGER NOT NULL DEFAULT 0`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `resolved_at TIMESTAMPTZ`

Indexes:
- `(status, severity, last_seen)`
- `(fingerprint, status)`

## 4. incident_audit_logs
- `id BIGSERIAL PRIMARY KEY`
- `incident_id BIGINT NOT NULL REFERENCES incidents(id)`
- `action VARCHAR(40) NOT NULL`
- `previous_status VARCHAR(20)`
- `new_status VARCHAR(20) NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

## Relationships
- One detection rule can trigger many incidents.
- One incident can have many audit entries.
- Incidents link to matching events through the fingerprint and evidence query (rule, source IP, and time range). A separate incident-event join table is optional, not required for the MVP.

## Implementation Notes
The SQL file is a reference schema. If Hibernate generates schema in development, ensure entity mappings and column names match the design. Avoid maintaining two conflicting schema sources. For a more controlled setup, implement Flyway migrations and use `ddl-auto=validate`.
