# Test Plan

## Unit Tests
1. Four failed logins within five minutes: no incident.
2. Fifth failed login within five minutes: incident created.
3. Failed login older than the window: excluded.
4. Events from two different source IPs: counted independently.
5. Disabled rule: no incident created.
6. Risk score remains between 0 and 100.
7. Invalid incident state transition: rejected.
8. Repeated matching events update active incident instead of duplicating it.

## Controller/API Tests
1. Valid event request returns 201.
2. Missing event type returns 400.
3. Invalid source IP returns 400.
4. Unknown incident ID returns 404.
5. Invalid status transition returns 409.
6. Unauthenticated access to protected management endpoint is denied.

## Persistence/Integration Tests
1. Event is persisted.
2. Incident and audit entry are committed consistently.
3. Dashboard totals match seeded data.
4. Restart does not erase PostgreSQL data.
5. Database failure is surfaced safely and does not return internals.

## Manual Acceptance Test
1. Start PostgreSQL and the app.
2. Open dashboard.
3. Run simulator for normal events.
4. Simulate five failed logins from one demo IP.
5. Confirm one incident appears with source, rule, window, and evidence count.
6. Submit more matching events and confirm incident updates.
7. Acknowledge and resolve incident.
8. Confirm audit history and persistence.

## Commands
```bash
./mvnw test
./mvnw clean package
```

On Windows, use `.\mvnw.cmd test` and `.\mvnw.cmd clean package`.

## Exit Criteria
All core detection tests pass, API validation works, PostgreSQL persistence is verified, the dashboard shows real stored data, and the documented demo can be repeated.
