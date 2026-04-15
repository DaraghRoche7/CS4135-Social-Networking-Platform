# API Contracts (Planned)

## Overview

This document describes the planned API contract structure for the Social Networking Platform.

## API Documentation

### Source of Truth
- **Swagger/OpenAPI** will be the source of truth for API contracts
- Each service will expose OpenAPI documentation at `/swagger-ui.html`
- API documentation will be automatically generated from code annotations

### API Gateway
- All client requests will go through the API Gateway
- Gateway will route to appropriate microservices
- Gateway URL will be the single entry point for the frontend

## Planned Endpoints

### Authentication Endpoints (Core Service)
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Token refresh
- `POST /api/auth/logout` - User logout

### User Endpoints (Core Service)
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile
- `GET /api/users/{id}/followers` - Get user followers
- `GET /api/users/{id}/following` - Get users following
- `POST /api/users/{id}/follow` - Follow a user
- `DELETE /api/users/{id}/follow` - Unfollow a user

### Post Endpoints (Core Service)
- `GET /api/posts` - Get posts (with pagination)
- `POST /api/posts` - Create a new post
- `GET /api/posts/{id}` - Get post by ID
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post
- `POST /api/posts/{id}/like` - Like a post
- `DELETE /api/posts/{id}/like` - Unlike a post

### Interaction Endpoints (Interaction Service)
- `POST /api/posts/{id}/like` - Like a post (returns 201, or 409 if already liked)
- `DELETE /api/posts/{id}/like` - Unlike a post (returns 204, or 404 if not liked)
- `GET /api/posts/{id}/likes` - Get like count and likedByCurrentUser flag

### Feed Endpoints (Support Service)
- `GET /api/feed` - Get user's personalized feed
- `GET /api/feed/timeline` - Get timeline feed

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

---

**Status:** Planned - Actual endpoints and contracts will be documented in Swagger once services are implemented.

