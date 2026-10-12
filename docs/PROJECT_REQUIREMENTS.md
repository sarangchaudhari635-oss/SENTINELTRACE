# Project Requirements Specification

## 1. Project Name
SentinelTrace: Real-Time Security Event Monitoring and Rule-Based Threat Detection.

## 2. Purpose
Demonstrate a Java web application that ingests application security events, detects configured patterns, persists incidents, and provides an investigation dashboard.

## 3. Users
- Student/demo administrator: reviews events, incidents, and rule configuration.
- Event producer or simulator: submits synthetic events through the API.

## 4. Functional Requirements
- FR-01: The system shall accept a security event with event type, source IP, event timestamp, and optional username/endpoint/details.
- FR-02: The system shall validate event requests before persistence.
- FR-03: The system shall persist accepted events in PostgreSQL.
- FR-04: The system shall evaluate failed-login events against a rolling time-window threshold.
- FR-05: The system should support a second HTTP request burst rule if core functionality is complete.
- FR-06: The system shall create or update an incident when a rule matches.
- FR-07: The system shall prevent repeated matching events from creating unbounded duplicate active incidents for the same rule and source.
- FR-08: The user shall be able to view and filter events and incidents.
- FR-09: The user shall be able to acknowledge and resolve incidents.
- FR-10: The system shall record incident status changes in an audit table.
- FR-11: The dashboard shall display event counts, open incident counts, severity distribution, and recent incidents.
- FR-12: The system shall provide a demo simulator that creates synthetic events only.

## 5. Non-Functional Requirements
- NFR-01: The app should start locally using documented steps.
- NFR-02: Detection rules must be deterministic and explainable.
- NFR-03: Persistence must survive application restarts.
- NFR-04: Invalid requests must return clear client errors without exposing stack traces.
- NFR-05: Controllers, services, repositories, and entities should have separate responsibilities.
- NFR-06: Core detection logic should have automated tests.
- NFR-07: Secrets must not be committed to source control.
- NFR-08: The MVP should be buildable in one day by keeping the feature set small.

## 6. Out of Scope
- Machine-learning threat detection.
- Production log-agent deployment.
- Microservices, Kafka, Kubernetes, and distributed processing.
- Automatic blocking, IP banning, or active response.
- Guaranteed real-time delivery or WebSocket push.
- Multi-tenant enterprise identity management.
- Claims that a matched rule proves an actual compromise.

## 7. Acceptance Criteria
- The application starts and connects to PostgreSQL.
- A valid event can be submitted and persisted.
- Four failed logins in the configured window do not trigger a five-event rule.
- The fifth matching failed login triggers an incident.
- An event outside the window does not count.
- Additional matching events update the active incident.
- An incident can be acknowledged and resolved.
- Each status change is audited.
- Dashboard data comes from persisted records.
- Automated tests pass and setup instructions are documented.
