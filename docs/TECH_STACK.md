# Technology Stack and Rationale

| Technology | Role | Why selected |
|---|---|---|
| Java 21 | Main language | Modern LTS Java baseline and strong OOP demonstration |
| Spring Boot 3.x | Application framework | Fast setup, dependency injection, MVC, configuration |
| Maven | Build and dependency management | Standard Java project workflow |
| Spring Web | REST and MVC routes | Event ingestion and browser controllers |
| Spring Data JPA | Persistence abstraction | Repository interfaces and query methods |
| Hibernate | ORM implementation | Maps Java entities to relational tables |
| PostgreSQL | Relational database | Durable data, indexes, transactions, constraints |
| Thymeleaf | Server-rendered pages | Avoids building and deploying a separate frontend |
| Bootstrap 5 | CSS/UI | Fast responsive dashboard layout |
| Chart.js | Charts | Simple event and severity visualizations |
| Jakarta Validation | Request validation | Declarative constraints on DTOs |
| Spring Security | Access control | Protect management routes and form actions |
| JUnit 5 | Unit tests | Test detection rules and service behavior |
| Mockito | Test doubles | Isolate service logic |
| MockMvc | HTTP tests | Exercise controller behavior |
| Git/GitHub | Version control | Track work and present the project |

## Recommended Dependencies
Generate the project at Spring Initializr and add:
- Spring Web
- Thymeleaf
- Spring Data JPA
- PostgreSQL Driver
- Validation
- Spring Security
- Spring Boot DevTools (optional)
- Spring Boot Starter Test

## Deliberate One-Day Tradeoffs
- Thymeleaf instead of React: one app and one build.
- Polling instead of WebSockets: simpler live-style updates.
- Synchronous detection instead of a queue: easier to reason about and test.
- Two simple deterministic rules instead of ML: evidence-first and explainable.
- Hibernate schema update for initial local setup; migrations can be added if time allows.
