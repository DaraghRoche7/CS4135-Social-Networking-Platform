# Architecture

## Overview

This document describes the prototype architecture implemented in this repository, plus a small amount of clearly-labelled future work.

## High-Level Architecture (Prototype)

### Frontend
- **React** application with functional components
- **Redux Toolkit** for state management
- **React Router** for client-side routing
- **Axios** for API communication

### Backend
- **Microservices architecture** with multiple Spring Boot services
- **API Gateway** (Spring Cloud Gateway) as the single entry point for the SPA
- **Message Broker** (RabbitMQ) provisioned for async communication
- **Redis** provisioned (not currently used for feed caching)

## Components

### API Gateway
- Central entry point for all client requests
- Routes requests to appropriate microservices
- Handles CORS configuration
- Masks underlying microservices architecture

#### Prototype routing (docker-compose)
- `/api/auth/**`, `/api/users/**` → **user-service**
- `/api/feed/**`, `/api/posts/**`, `/api/modules/**` → **core-service**
- `/api/notifications/**` → **support-service**

### Microservices
- **User Service**: authentication + identity (register/login/refresh/logout, user profile + follow)
- **Core Service**: posts + module follow + feed reads, plus likes/comments/upload within the prototype
- **Support Service**: notifications APIs and background processing

### Data Layer
- **PostgreSQL** for persistent data storage
- **Redis** is provisioned but not currently used to serve feed reads (PostgreSQL is the source for feed queries)
- **RabbitMQ** for event-driven messaging

## Patterns

- **Layered Architecture**: Controller → Service → Repository
- **CQRS (lightweight)**: read endpoints use repository queries; write flows remain within service boundaries
- **Event-Driven (selective)**: RabbitMQ is available; not all bounded-context integrations are implemented in the prototype
- **Microservices**: Independent, scalable services

## Future work (explicitly not required for prototype)
- **Rate limiting** at gateway
- **Horizontal scaling**: multiple gateway replicas behind a load balancer
- **Redis cache-aside** for feed reads (see `Authentication-Security.md` and `Services.md` for current behaviour)

