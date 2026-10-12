# Viva Preparation

## 60-Second Explanation
SentinelTrace is a Java web application built with Spring Boot and PostgreSQL. It collects normalized security events and checks them against deterministic time-window rules. When a rule threshold is met, the system creates or updates an incident, stores its evidence context, and displays it on a dashboard. Incident state changes are recorded in an audit table. The project demonstrates OOP, REST APIs, persistence, validation, SQL queries, transactions, and unit testing.

## Likely Questions
### Why Java and Spring Boot?
Java demonstrates OOP and strong typing. Spring Boot reduces setup work and provides dependency injection, MVC, validation integration, and database support.

### Why PostgreSQL?
It provides durable relational storage, constraints, indexes, and transactions for events and incidents.

### What is JPA/Hibernate?
JPA is the Java persistence specification; Hibernate is an implementation that maps entities to relational tables.

### How does the brute-force rule work?
For a failed-login event, count matching events from the same source IP inside a rolling time window. If the count meets the configured threshold, create or update an incident.

### Why use event timestamps?
They allow the system to evaluate a rolling window rather than count all historical failures. Production systems must also define policies for late or out-of-order events.

### How are duplicate incidents prevented?
A stable fingerprint groups detections by rule and source. If an active incident exists for that fingerprint, update it rather than creating another incident.

### Does an alert prove an attack occurred?
No. It proves the configured rule matched observed or simulated events. False positives are possible.

### What are the limitations?
The MVP has simple rules, a single application instance, limited event sources, and no distributed queue or high availability.

### How could it be improved?
Add real log adapters, stronger concurrent deduplication, retention policies, rule versioning, WebSocket delivery, deployment automation, and broader integration tests.

## Demo Checklist
- App and database start.
- Dashboard loads.
- Simulated failed logins trigger one incident.
- Incident includes a clear reason and evidence count.
- Incident can be acknowledged and resolved.
- Audit history persists.
- Tests pass.
