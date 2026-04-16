# StudyHub Backend

This backend is split into three Spring Boot applications:

- `core-service` on port `8081` issues JWTs, exposes user/profile APIs, and powers the post/feed workflow.
- `support-service` on port `8082` serves notifications and consumes login events.
- `api-gateway` on port `8080` is the single frontend entry point and routes `/api/auth/**`, `/api/feed/**`, `/api/posts/**`, `/api/users/**`, and `/api/notifications/**`.

## What is implemented

- Layered architecture in both services: controller, service, repository.
- DTO-based request and response models with Bean Validation.
- JWT-based stateless authentication for user APIs.
- RBAC using `@PreAuthorize`.
- PDF note upload, personalized feeds, likes, follows, and profile update APIs.
- Service-to-service REST from `support-service` to `core-service` for recipient validation.
- RabbitMQ event publishing from `core-service` and event consumption in `support-service`.
- JPA + Flyway with H2 defaults and PostgreSQL-ready settings.
- Global JSON exception handling via `@RestControllerAdvice`.
- OpenAPI/Swagger in both services.

## Demo credentials

- Student: `student@studyhub.local` / `Password123!`
- Peer student: `peer@studyhub.local` / `Password123!`
- Admin: `admin@studyhub.local` / `Password123!`

These demo users are seeded outside the `prod` profile.

## Local run

1. Start `core-service`
2. Start `support-service`
3. Start `api-gateway`
4. Start the React frontend and point it to `http://localhost:8080`

You can run each service with:

```powershell
.\mvnw.cmd spring-boot:run
```

## Swagger

- Core service: `http://localhost:8081/swagger-ui.html`
- Support service: `http://localhost:8082/swagger-ui.html`

## Demo API surface

- `POST /api/auth/login`
- `GET /api/feed`
- `GET /api/posts`
- `POST /api/posts`
- `POST /api/posts/{postId}/like`
- `DELETE /api/posts/{postId}/like`
- `GET /api/users/{userId}`
- `PUT /api/users/{userId}`
- `POST /api/users/{userId}/follow`
- `DELETE /api/users/{userId}/follow`
- `GET /api/notifications`
- `PUT /api/notifications/{notificationId}/read`

## Docker Compose

`docker-compose.yml` provisions PostgreSQL, RabbitMQ, both services, and the API gateway. It is intended for a full local stack demo with seeded users and real broker-backed messaging.

## Notes

- The `prod` profile disables Swagger UI and demo-token behavior.
- `APP_JWT_SECRET` and `APP_INTERNAL_API_KEY` must be overridden in production.
- Gateway CORS is configured for the Vite frontend on `http://localhost:5173`.
- `APP_POSTS_DIR` controls where uploaded PDFs are stored; Docker Compose mounts that path to a named volume for persistence.
