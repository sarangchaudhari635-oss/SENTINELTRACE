# API Specification (MVP)

Base URL: `http://localhost:8080`

## POST /api/events
Accept one event.

Request:
```json
{
  "eventType": "LOGIN_FAILURE",
  "sourceIp": "192.0.2.15",
  "username": "demo_user",
  "endpoint": "/login",
  "occurredAt": "2026-10-12T16:30:00Z",
  "details": "Invalid credentials"
}
```
Expected: `201 Created` with stored event or a stable response object. Invalid input: `400 Bad Request`.

## GET /api/events
Returns paginated events. Suggested query parameters: `page`, `size`, `eventType`, `sourceIp`, `from`, `to`.

## GET /api/incidents
Returns incidents. Suggested query parameters: `status`, `severity`, `page`, `size`.

## GET /api/incidents/{id}
Returns incident details, matched rule metadata, and evidence events.

## PATCH /api/incidents/{id}/status
Request:
```json
{ "status": "ACKNOWLEDGED" }
```
Allowed values: `ACKNOWLEDGED`, `RESOLVED`. Validate the state transition and write an audit record. Unknown ID: `404`; invalid transition: `409`.

## GET /api/dashboard/summary
Returns event totals, failed-login totals, open incident totals, severity counts, and chart series.

## POST /api/demo/simulate
Generates synthetic demo events. Keep this endpoint local/demo-only or protect it with authorization. It must not initiate network activity against real hosts.

## General API Requirements
- Use DTOs and Bean Validation.
- Return consistent errors.
- Use pagination and enforce maximum page size.
- Do not expose stack traces, secrets, or full entity internals.
- Protect state-changing endpoints.
- Ensure the implemented API matches this document; revise this spec if the actual route names differ.
