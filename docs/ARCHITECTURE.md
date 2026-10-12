# Architecture and Design

## Architecture Style
A layered monolith. Keep one deployable Spring Boot application, with distinct controller, service, repository, and domain layers.

## Logical Diagram
```text
Browser
  |-- Thymeleaf / Bootstrap / Chart.js
  v
Controllers (MVC pages + REST endpoints)
  v
Services
  |-- EventIngestionService
  |-- DetectionEngine
  |-- IncidentService
  |-- AnalyticsService
  |-- DemoEventSimulator
  v
Spring Data JPA Repositories
  v
PostgreSQL
```

## Event Processing Sequence
1. Controller receives request DTO.
2. Bean Validation checks required values and allowed ranges.
3. Ingestion service normalizes and persists the event.
4. Detection engine loads enabled rules relevant to the event.
5. Engine queries matching events in the configured time window.
6. On a match, incident service finds an existing active incident fingerprint or creates one.
7. Incident state changes are persisted with an audit entry.
8. Dashboard reads summary and history from repositories.

## Core Design Decisions
- Controllers should not contain detection or database business logic.
- DTOs should be distinct from JPA entities.
- Use enums for event type, severity, and incident status.
- Use transactions around event processing and incident/audit updates.
- Use database indexes for source IP, event type, and event timestamp.
- Make rules configurable but do not build a generic scripting language.
- Explain every alert with rule code, time window, event count, source, and timestamps.

## Error Handling
Use a global exception handler with stable JSON error shapes for REST requests. Return 400 for invalid payloads, 404 for unknown incident IDs, 409 for invalid state transitions where appropriate, and 500 for unexpected errors without exposing internals.

## Incident Deduplication
Build a stable fingerprint from rule code and normalized source IP (and account where appropriate). Search for an active incident with that fingerprint and update it if found. For the one-day MVP, synchronous processing and a transaction may be adequate. If concurrent ingestion is implemented, use a database uniqueness/locking strategy and test races.

## Security Boundaries
- Use synthetic events for demonstrations.
- Require authentication for management operations if the application is accessible beyond localhost.
- Keep CSRF enabled for browser forms.
- Validate IP and event type; cap string lengths and batch sizes.
- Do not include passwords or session tokens in event details.
