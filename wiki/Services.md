# Services

This document outlines the services implemented in the prototype, their responsibilities, and key cross-service boundaries (as routed by the API gateway).

## User Service (`services/user-service`)

### Responsibilities
- Authentication and identity:
  - register / login / refresh / logout
  - password reset endpoints
- User profile APIs
- Follow/unfollow user relationships

### API surface (via gateway)
- `/api/auth/**`
- `/api/users/**`

### Technology Stack
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA + PostgreSQL
- Bean Validation

### Key Features
- Issues stateless JWT access tokens and stateful refresh tokens
- UL-only domain rule for registration (`@ul.ie` or `@studentmail.ul.ie`)
- Demo users are seeded in non-prod to unblock review/testing

---

## Core Service (`backend/core-service`)

### Responsibilities
- Post/feed domain for the prototype:
  - feed reads
  - posts upload/listing
  - likes and comments (prototype consolidation)
  - module follow (for feed scoping)

### API surface (via gateway)
- `/api/feed/**`
- `/api/posts/**`
- `/api/modules/**`

### Technology Stack
- Spring Boot
- Spring Security (JWT validation for protected APIs)
- Spring Data JPA + PostgreSQL
- Flyway migrations
- RabbitMQ wiring exists; not all integrations are implemented

### Key Features
- Feed queries are served from PostgreSQL (no Redis cache-aside in current implementation)
- Uses `X-User-Id` header (temporary) for identifying the viewer in the prototype

---

## Support Service (`backend/support-service`)

### Responsibilities
- Notification APIs for the SPA
- Background processing / consumption of selected events (prototype-oriented)
- Validates notification recipients by calling Core Service internal endpoints

### API surface (via gateway)
- `/api/notifications/**`

### Resilience note (implemented)
- Outbound Core validation calls are protected with:
  - connect/read timeouts
  - small bounded retry for transient failures (5xx/429 and network/timeouts)

---

## API Gateway

### Responsibilities
- Central entry point for all requests
- Request routing to appropriate services
- CORS configuration
- Request/response transformation

### Technology Stack
- Spring Cloud Gateway
- Routing + CORS (prototype)
- Rate limiting (future work)

### Key Features
- Dynamic routing
- Request filtering

## Redis note (prototype behaviour)
- Redis is provisioned in docker-compose, but feed reads currently hit PostgreSQL (no caching yet).

