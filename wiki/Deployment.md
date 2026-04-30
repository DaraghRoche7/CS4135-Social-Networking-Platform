# Deployment

## Overview

This document describes how to run the prototype locally for review/testing.

## Local prototype run

### Option A: Full Docker (may require stable image pulls)
- Build services (JARs) and run `docker compose up -d --build`

### Option B (recommended if Docker image pulls are flaky): Docker infra + services local
Run infrastructure services in Docker, and run Spring services locally.

1) Start infra containers:
- PostgreSQL (port 5432)
- Redis (port 6379) – provisioned (not required for feed reads)
- RabbitMQ (ports 5672, 15672)

2) Run Spring services locally:
- API Gateway: `http://localhost:8080`
- Core Service: `http://localhost:8081`
- Support Service: `http://localhost:8082`
- User Service: `http://localhost:8083`

3) Run frontend locally:
- Vite dev server: `http://localhost:5173`
- Frontend calls the gateway base URL (default `http://localhost:8080`)

### Smoke test (via gateway)
- Login:
  - `POST /api/auth/login`
- Feed:
  - `GET /api/feed`

## Future work (production)
- CI/CD pipeline (GitHub Actions) for build/test/deploy
- Observability (metrics/logging)
- Gateway rate limiting + horizontal scaling

