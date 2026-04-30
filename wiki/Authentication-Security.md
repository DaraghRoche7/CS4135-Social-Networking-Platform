# Authentication & Security

## Overview

This document describes the authentication and security implementation used by the prototype deployment.

## Authentication Strategy

### JWT (JSON Web Tokens)
- **Stateless authentication** using JWT tokens
- Tokens will contain user ID, roles, and expiration time
- Access tokens for API requests
- Refresh tokens for token renewal

#### Ownership (prototype)
- All `/api/auth/**` endpoints are owned by **user-service** and are accessed via the API gateway.
- The Core Service contains a `/api/auth/login` endpoint, but it is not part of the main gateway-auth flow.

### Token Flow
1. User logs in (`POST /api/auth/login`) via the API gateway → user-service
2. user-service validates credentials and issues:
   - access token (JWT)
   - refresh token (stored server-side in the user-service DB and returned to the client)
3. Frontend stores tokens and includes:
   - `Authorization: Bearer <JWT>` on API requests
   - `X-User-Id` temporarily (identity is not injected by the gateway yet)
4. On 401 responses (non-auth endpoints), the frontend calls:
   - `POST /api/auth/refresh` and retries the original request if refresh succeeds

## Security Implementation

### Backend (Spring Security)
- JWT validation is enforced within downstream services (not centrally at the gateway):
  - core-service enforces JWT on protected business APIs
  - support-service enforces JWT on notification APIs
- Password hashing uses BCrypt in user-service

### Frontend (React)
- **Axios interceptors** for token handling
- Automatic token inclusion in requests
- 401 error handling and redirect to login
- Token expiration detection and refresh
- Secure token storage

#### Registration payload (user-service)
`POST /api/auth/register` accepts:
```json
{
  "name": "Student Name",
  "email": "student@studentmail.ul.ie",
  "password": "Password123!"
}
```

Constraints:
- UL-only email domain rule: `@ul.ie` or `@studentmail.ul.ie`
- Password length ≥ 8 characters

## Role-Based Access Control (RBAC)

### Roles (Planned)
- **STUDENT**: Standard user role
- **ADMIN**: Administrative privileges

### Implementation
- `@PreAuthorize` annotations on protected endpoints
- Role checks in service layer
- Frontend route protection based on roles

### Protected Endpoints
- Admin-only endpoints (e.g., delete resources)
- User-specific endpoints (e.g., update own profile)
- Public endpoints (e.g., view public posts)

## Security Features

### Input Validation
- Bean Validation annotations on DTOs
- Server-side validation for all inputs
- SQL injection prevention via parameterized queries
- XSS prevention through proper encoding

### CORS Configuration
- Explicit CORS configuration in API Gateway
- Allowed origins for React frontend
- Credential handling for authenticated requests

### Password Security
- BCrypt hashing with salt
- Password strength requirements
- No password storage in plain text

## Configuration notes
- Access token TTL is configured via `JWT_EXPIRATION_MS` (default 86400000ms / 24 hours in docker-compose)

## Future work
- Gateway rate limiting
- Optional edge JWT validation (only if identity propagation is designed end-to-end)

