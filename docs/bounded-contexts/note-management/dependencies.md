# Note Management — Dependencies

## Runtime Stack

| Component | Version |
|---|---|
| Java | 17 (LTS) |
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.1 |
| Maven | 3.x |

---

## Library Dependencies

### Spring Boot Starters

| Dependency | Version | Purpose |
|---|---|---|
| `spring-boot-starter-web` | 3.2.5 | Embedded Tomcat, REST controllers, multipart upload |
| `spring-boot-starter-data-jpa` | 3.2.5 | Hibernate ORM, `JpaRepository`, `@Transactional` |
| `spring-boot-starter-amqp` | 3.2.5 | RabbitMQ client via `RabbitTemplate` — async event publishing |
| `spring-boot-starter-security` | 3.2.5 | Security filter chain, JWT filter integration |
| `spring-boot-starter-validation` | 3.2.5 | Bean Validation (`@NotBlank`, `@Size`) on DTOs |
| `spring-boot-starter-actuator` | 3.2.5 | `/actuator/health` and metrics endpoints |

### Spring Cloud

| Dependency | Version | Purpose |
|---|---|---|
| `spring-cloud-starter-netflix-eureka-client` | 2023.0.1 | Service registration and discovery with Eureka Server |
| `spring-cloud-starter-config` | 2023.0.1 | Loads centralised configuration from Config Server at startup |

### Resilience

| Dependency | Version | Purpose |
|---|---|---|
| `resilience4j-spring-boot3` | 2.2.0 | `@CircuitBreaker` annotations on event publishing methods |
| `resilience4j-reactor` | 2.2.0 | Reactive support required by resilience4j-spring-boot3 |

### Security / JWT

| Dependency | Version | Purpose |
|---|---|---|
| `jjwt-api` | 0.12.5 | JWT parsing API — `Jwts.parser()`, `Claims` |
| `jjwt-impl` | 0.12.5 | Runtime JWT implementation (scope: runtime) |
| `jjwt-jackson` | 0.12.5 | Jackson-based JWT serialisation (scope: runtime) |

### Database

| Dependency | Version | Purpose |
|---|---|---|
| `postgresql` | managed by Boot | JDBC driver for PostgreSQL (production, scope: runtime) |
| `h2` | managed by Boot | In-memory database for local dev and tests |

### Utilities

| Dependency | Version | Purpose |
|---|---|---|
| `lombok` | 1.18.36 | Compile-time code generation — `@Getter`, `@Builder`, `@Data`, etc. Excluded from final JAR |

### Test

| Dependency | Version | Scope |
|---|---|---|
| `spring-boot-starter-test` | 3.2.5 | `test` — JUnit 5, Mockito, MockMvc |
| `spring-security-test` | managed by Boot | `test` — `@WithMockUser`, `SecurityMockMvcRequestPostProcessors` |

---

## Infrastructure Dependencies

These are external services the Note Service connects to at runtime. All are **optional in the `local` profile** — the service starts and functions without them.

### PostgreSQL

| Property | Value |
|---|---|
| Host | `localhost:5432` (production) |
| Database | `studyhub_notes` |
| Username | `studyhub` |
| DDL strategy | `update` (production) / `create-drop` (local H2) |
| Profile | Required in `default`; replaced by H2 in `local` |

The `notes` table is the only table managed by this service. Schema is auto-managed by Hibernate.

### RabbitMQ

| Property | Value |
|---|---|
| Host | `localhost:5672` |
| Credentials | `guest / guest` (dev) |
| Exchange | `studyhub.notes` (topic exchange) |
| Routing keys | `note.uploaded`, `note.downloaded`, `note.deleted` |
| Profile | Auto-configured excluded in `local` profile via `RabbitAutoConfiguration` exclusion |
| Failure handling | Resilience4j circuit breaker — if RabbitMQ is unavailable, events are logged and the operation still succeeds |

### Netflix Eureka (Service Discovery)

| Property | Value |
|---|---|
| Server URL | `http://localhost:8761/eureka/` |
| Registered name | `note-service` |
| Profile | Disabled (`eureka.client.enabled=false`) in `local` profile |
| Purpose | Allows other services (Feed, Notifications) to resolve `note-service` by name rather than hardcoded URL |

### Spring Cloud Config Server

| Property | Value |
|---|---|
| Server URL | `http://localhost:8888` |
| Bootstrap config | `bootstrap.yml` — loaded before application context |
| Profile | Skipped (`spring.config.import=""`) in `local` profile |
| Purpose | Centralises configuration across all StudyHub microservices |

---

## Inter-Service Dependencies

The Note Service does **not** make synchronous HTTP calls to any other bounded context. All cross-context communication is asynchronous via RabbitMQ domain events.

### Upstream (what Note Service consumes)

| Service | How | What |
|---|---|---|
| **User/Auth Service** (Daragh Roche) | JWT token in `Authorization` header | `userId`, `role`, `username` claims extracted at request time by `JwtAuthenticationFilter` — no HTTP call made |

### Downstream (what Note Service produces)

| Event | Consumed by | Trigger |
|---|---|---|
| `NoteUploadedEvent` | Feed Service (Zeba Marium), Notifications Service (Nur Alislam) | Successful note upload |
| `NoteDownloadedEvent` | Feed Service | Successful note download |
| `NoteDeletedEvent` | Feed Service, Notifications Service | Successful note deletion |

> The Note Service has no compile-time or runtime dependency on any other StudyHub microservice. Coupling is limited to the shared JWT secret and the agreed RabbitMQ exchange/routing key naming convention.

---

## Profile Summary

| Profile | Database | RabbitMQ | Eureka | Config Server |
|---|---|---|---|---|
| `default` (production) | PostgreSQL | Enabled | Enabled | Enabled |
| `local` (dev) | H2 in-memory | Disabled | Disabled | Disabled |
| `test` | H2 in-memory | Disabled | Disabled | Disabled |

---

## Dependency Graph

```
                        ┌─────────────────────────┐
                        │      note-service        │
                        │      (port 8082)         │
                        └────────────┬────────────┘
                                     │
             ┌───────────────────────┼───────────────────────┐
             │                       │                       │
             ▼                       ▼                       ▼
    ┌────────────────┐    ┌─────────────────────┐  ┌──────────────────┐
    │  PostgreSQL    │    │     RabbitMQ         │  │  Eureka Server   │
    │  :5432         │    │     :5672            │  │  :8761           │
    │  studyhub_notes│    │  exchange:           │  │  (discovery)     │
    └────────────────┘    │  studyhub.notes      │  └──────────────────┘
                          └──────────┬──────────┘
                                     │ publishes events
                    ┌────────────────┼────────────────┐
                    ▼                ▼                 ▼
           ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
           │ Feed Service │  │Notifications │  │  Likes Service   │
           │ (Zeba)       │  │(Nur Alislam) │  │  (Paudie)        │
           └──────────────┘  └──────────────┘  └──────────────────┘

    JWT secret ──────────────────────────────────────────────────────►
    (shared)         User/Auth Service (Daragh)  ──► issues tokens
                     note-service validates them locally — no HTTP call
```
