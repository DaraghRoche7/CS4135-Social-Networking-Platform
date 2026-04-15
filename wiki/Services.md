# Services (Planned)

This document outlines the planned services and their responsibilities.

## Core Service

### Responsibilities
- User registration and authentication
- User profile management
- Post creation, editing, and deletion
- Like/unlike functionality
- Follow/unfollow functionality

### Technology Stack
- Spring Boot 3.x
- Spring Security with JWT
- Spring Data JPA with PostgreSQL
- Bean Validation for input validation

### Key Features
- RESTful API endpoints
- JWT token generation and validation
- Role-Based Access Control (RBAC)
- DTOs for all API requests/responses
- Global exception handling

---

## Support Service

### Responsibilities
- Feed generation using CQRS pattern
- Timeline caching with Redis
- Notification management
- Event-driven fan-out for followers

### Technology Stack
- Spring Boot 3.x
- Redis for caching
- RabbitMQ for event consumption
- Spring Data JPA with PostgreSQL

### Key Features
- Personalized feed generation
- Redis caching for performance
- Event-driven architecture
- Fan-out pattern implementation

---

## API Gateway

### Responsibilities
- Central entry point for all requests
- Request routing to appropriate services
- CORS configuration
- Request/response transformation

### Technology Stack
- Spring Cloud Gateway
- Load balancing
- Rate limiting (planned)

### Key Features
- Dynamic routing
- Service discovery integration
- Request filtering

---

**Status:** Planned - Service responsibilities will be refined during implementation.

