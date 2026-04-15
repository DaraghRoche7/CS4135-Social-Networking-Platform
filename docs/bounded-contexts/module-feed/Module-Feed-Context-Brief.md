# Module feed and post engagement (bounded context)

## Purpose

The **module feed** gives each student a list of **posts** for modules they follow. The same service handles **following modules**, **uploading posts** (with an optional PNG), **likes**, and **comments**. Implementation: **`support-service`** (Java/Spring), data in **PostgreSQL**, exposed through the **API gateway** to the **React** frontend.

## In scope

- Personalised feed: posts filtered by followed modules, or by one module if the client passes a filter.
- Module follow / unfollow per user (`user_modules`).
- Post rows: title, description, module code, uploader id, timestamps, denormalised like count, optional attachment filename.
- Likes and comments on those posts.

## Out of scope

- User profiles and JWT issuance (user service); this context only receives **user id** (header `X-User-Id` until the gateway injects it from a token).
- PDF **notes** catalogue and downloads (**note-service**, `/api/notes`) — separate from feed **posts** (`/api/posts`).

## Main rules (behaviour)

- Without a module filter, the feed only shows posts whose **module code** is in the user’s **followed** list. If the user follows nothing, the feed is empty.
- With a **module** query parameter, results are limited to that module.
- Ordering: posts the user has **not** yet liked or commented on appear **first**; posts they have engaged with appear **after**; within each group, **newest first** (`createdAt` descending). (`PostRepository.findFeedPosts`.)
- Likes are idempotent; like count on the post is updated with each like/unlike.

## API summary (via gateway, typical base `http://localhost:8080`)

| Method | Path | Notes |
|--------|------|--------|
| GET | `/api/feed` | Query: `module`, `page`, `size`. Header: `X-User-Id`. |
| GET | `/api/modules` | List followed module codes. Header: `X-User-Id`. |
| POST | `/api/modules/follow` | JSON body with `moduleCode`. Header: `X-User-Id`. |
| DELETE | `/api/modules/{moduleCode}` | Unfollow. Header: `X-User-Id`. |
| POST | `/api/posts` | Multipart: title, description, module, PNG file. Header: `X-User-Id`. |
| POST / DELETE | `/api/posts/{id}/like` | Header: `X-User-Id`. |
| GET / POST | `/api/posts/{id}/comments` | POST needs `X-User-Id` and JSON `{ "body": "..." }`. |

Feed response shape: `items` (array of posts with `likesCount`, `likedByUser`, etc.), `page`, `size`, `total`.

## Technical dependencies

| Type | What |
|------|------|
| Runtime | Java 21, Spring Boot 3.3 |
| Database | PostgreSQL (`studyhub`), JPA/Hibernate |
| Cache infra | Redis (configured for caching; feed query path is not annotated as cached) |
| Files | Upload directory `app.upload.dir` for PNG attachments |
| Clients | SPA (`StudentDashboard`), optional `TestDataSeeder` via gateway |

## Cross-service dependencies

| Direction | Dependency | How |
|-----------|------------|-----|
| Upstream | User / auth | Identity as string user id (`X-User-Id`); no HTTP call from support-service to user-service. |
| Upstream | API gateway | Routes `/api/feed`, `/api/modules`, `/api/posts` to support-service; CORS for the frontend. |
| Sibling | Note service | Separate routes (`/api/notes/**`); different data model. |
| Sibling | Interaction service | Gateway may route `/api/interactions/**`; this project’s feed UI uses support-service like/comment endpoints under `/api/posts/...`. |
| Downstream | Frontend | Axios to gateway; sends Bearer token and `X-User-Id` from storage. |

**Deployment note:** In default gateway YAML, note-service and support-service URIs may both point to port 8082; use environment variables so each service has its own address when both run.

---

*Paste into Word: open this file in Word or copy from a Markdown preview; adjust heading styles if needed.*
