# API Contracts

## Overview

This document describes the API contracts exposed by the prototype (via the API gateway), plus a small set of clearly-labelled future work.

## API Documentation

### Source of Truth
- **Swagger/OpenAPI** is the source of truth for API contracts
- Each service will expose OpenAPI documentation at `/swagger-ui.html`
- API documentation will be automatically generated from code annotations

### API Gateway
- All client requests will go through the API Gateway
- Gateway will route to appropriate microservices
- Gateway URL will be the single entry point for the frontend

## Prototype Endpoints (via API Gateway)

### Authentication Endpoints (user-service)
- `POST /api/auth/register` - User registration (UL-only domains)
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Token refresh (rotates refresh token)
- `POST /api/auth/logout` - Logout (invalidates refresh token)
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password with token

### User Endpoints (user-service)
- `GET /api/users/{userId}` - Get user profile
- `PUT /api/users/{userId}` - Update profile
- `POST /api/users/{userId}/follow` - Follow a user
- `DELETE /api/users/{userId}/follow` - Unfollow a user

### Post Endpoints (core-service)
- `GET /api/posts` - List posts
- `POST /api/posts` - Create a new post (PNG upload supported in prototype)
- `POST /api/posts/{postId}/like` - Like a post
- `DELETE /api/posts/{postId}/like` - Unlike a post
- `POST /api/posts/{postId}/comments` - Add a comment
- `GET /api/posts/{postId}/comments` - List comments

### Module Endpoints (core-service)
- `GET /api/modules` - List modules
- `POST /api/modules/follow` - Follow a module

### Feed Endpoints (core-service)
- `GET /api/feed` - Get personalized feed (served from PostgreSQL; no Redis caching yet)

### Notification Endpoints (Support Service)
- `GET /api/notifications` - Get user notifications
- `PUT /api/notifications/{id}/read` - Mark notification as read

## Request/Response Format

### DTOs
- All API requests and responses will use Data Transfer Objects (DTOs)
- DTOs will be separate from internal entities
- DTOs will include validation annotations

### Error Responses
- Standardized error response format
- HTTP status codes following REST conventions
- Meaningful error messages

## Notes
- The API gateway is the single frontend entry point (default `http://localhost:8080`).
- Swagger endpoints are exposed per service (see `Deployment.md` / service configs).

