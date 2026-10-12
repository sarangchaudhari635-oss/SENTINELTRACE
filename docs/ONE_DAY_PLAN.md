# One-Day Implementation Plan

The one-day constraint requires strict prioritization. Finish one end-to-end path before adding polish.

## Phase 1: Setup (45–60 minutes)
- Generate Spring Boot project.
- Add required dependencies.
- Create PostgreSQL database.
- Configure environment variables.
- Verify app starts.

## Phase 2: Persistence and event ingestion (90 minutes)
- Create `SecurityEvent` entity and repository.
- Create event DTO with validation.
- Implement `POST /api/events`.
- Test using Postman or curl.

## Phase 3: Detection (90–120 minutes)
- Seed one failed-login rule.
- Query events by source IP, event type, and time window.
- Create/update incidents transactionally.
- Add fingerprint-based deduplication.
- Write unit tests for below-threshold, threshold, and expired-window cases.

## Phase 4: Incident workflow (45–60 minutes)
- Implement incident list/detail.
- Add acknowledge and resolve actions.
- Persist audit records.

## Phase 5: Dashboard (90–120 minutes)
- Build a simple Thymeleaf/Bootstrap dashboard.
- Show event count, open incidents, severity, and recent events.
- Add incident actions and a simulator button.
- Prefer clear functionality over elaborate animations.

## Phase 6: Verification and submission (60–90 minutes)
- Run tests.
- Fix critical defects.
- Verify clean startup.
- Capture screenshots.
- Complete README, architecture, schema, API, and test documentation.
- Rehearse the demo.

## Cut Line
If behind schedule, omit the HTTP burst rule, editable rule management, advanced charts, and polling. Do not omit persistence, one working detection rule, incident details, or the test/demo path.
