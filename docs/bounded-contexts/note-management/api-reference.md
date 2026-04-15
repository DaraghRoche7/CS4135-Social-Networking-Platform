# Note Management — API Reference

**Base URL:** `http://localhost:8082`  
**API Prefix:** `/api/notes`  
**Content-Type:** `application/json` (except uploads — `multipart/form-data`)

---

## Authentication

All write endpoints (`POST`, `DELETE`) and `GET /my` require a JWT Bearer token.

```
Authorization: Bearer <token>
```

The token is issued by the **User/Auth** bounded context (shared secret: `studyhub-secret-key-for-jwt-authentication-2024`).  
In **local dev** mode a test token can be obtained without credentials:

```
GET /api/dev/token?username=test-user&role=STUDENT
```

The JWT payload must contain:
| Claim | Type | Description |
|---|---|---|
| `sub` | UUID string | User ID — used as `uploaderId` / `requesterId` |
| `username` | String | Display name |
| `role` | String | `STUDENT` or `ADMIN` |

---

## Endpoints

### 1. Upload a Note

```
POST /api/notes
```

**Auth required:** Yes  
**Content-Type:** `multipart/form-data`

Uploads a PDF study note. The request must include two named parts: `metadata` (JSON) and `file` (PDF binary).

#### Request Parts

| Part | Type | Required | Description |
|---|---|---|---|
| `metadata` | JSON object | Yes | Note metadata (see schema below) |
| `file` | Binary (PDF) | Yes | The PDF file to upload |

#### `metadata` Schema

```json
{
  "title":       "string (3–200 chars, required)",
  "description": "string (max 2000 chars, optional)",
  "moduleCode":  "string (2–20 chars, required)",
  "moduleName":  "string (max 100 chars, optional)"
}
```

#### Example Request (curl)

```bash
curl -X POST http://localhost:8082/api/notes \
  -H "Authorization: Bearer <token>" \
  -F 'metadata={"title":"Week 3 Lecture Notes","moduleCode":"CS4135","moduleName":"Software Architectures"};type=application/json' \
  -F 'file=@/path/to/notes.pdf;type=application/pdf'
```

#### Response — `201 Created`

```json
{
  "id":            "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "uploaderId":    "a1b2c3d4-0000-0000-0000-000000000001",
  "title":         "Week 3 Lecture Notes",
  "description":   null,
  "moduleCode":    "CS4135",
  "moduleName":    "Software Architectures",
  "fileName":      "notes.pdf",
  "fileSize":      204800,
  "mimeType":      "application/pdf",
  "downloadCount": 0,
  "status":        "ACTIVE",
  "createdAt":     "2026-04-14T12:00:00",
  "updatedAt":     "2026-04-14T12:00:00"
}
```

#### Error Responses

| Status | Reason |
|---|---|
| `400 Bad Request` | Non-PDF file, file exceeds 10 MB, or metadata validation failure |
| `401 Unauthorized` | Missing or invalid JWT |

---

### 2. Get Note Metadata

```
GET /api/notes/{noteId}
```

**Auth required:** No

Returns metadata for a single active note. Does not serve the file.

#### Path Parameters

| Parameter | Type | Description |
|---|---|---|
| `noteId` | UUID | The note's unique identifier |

#### Example Request

```bash
curl http://localhost:8082/api/notes/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

#### Response — `200 OK`

Same schema as the upload response above.

#### Error Responses

| Status | Reason |
|---|---|
| `404 Not Found` | Note does not exist or has been deleted/flagged |

---

### 3. Download a Note (PDF)

```
GET /api/notes/{noteId}/download
```

**Auth required:** No (anonymous downloads are tracked with a random ID)

Streams the PDF file as a binary attachment. Increments the note's `downloadCount` atomically and publishes a `NoteDownloadedEvent`.

#### Path Parameters

| Parameter | Type | Description |
|---|---|---|
| `noteId` | UUID | The note's unique identifier |

#### Example Request

```bash
curl -O -J http://localhost:8082/api/notes/3fa85f64-5717-4562-b3fc-2c963f66afa6/download \
  -H "Authorization: Bearer <token>"
```

#### Response — `200 OK`

```
Content-Type: application/pdf
Content-Disposition: attachment; filename="notes.pdf"
<binary PDF body>
```

#### Error Responses

| Status | Reason |
|---|---|
| `404 Not Found` | Note does not exist, has been deleted, or file is missing from disk |

---

### 4. Get My Notes

```
GET /api/notes/my
```

**Auth required:** Yes

Returns a paginated list of notes uploaded by the authenticated user. Only `ACTIVE` notes are returned.

#### Query Parameters

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | Zero-based page index |
| `size` | int | `20` | Page size |
| `sort` | string | — | e.g. `createdAt,desc` |

#### Example Request

```bash
curl "http://localhost:8082/api/notes/my?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

#### Response — `200 OK`

```json
{
  "content": [ /* array of NoteResponse */ ],
  "totalElements": 5,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true
}
```

#### Error Responses

| Status | Reason |
|---|---|
| `401 Unauthorized` | Missing or invalid JWT |

---

### 5. Browse Notes by Module

```
GET /api/notes/module/{moduleCode}
```

**Auth required:** No

Returns a paginated list of active notes for a given academic module. Supports sorting by recency or popularity.

#### Path Parameters

| Parameter | Type | Description |
|---|---|---|
| `moduleCode` | String | Module code to filter by (e.g. `CS4135`) |

#### Query Parameters

| Parameter | Type | Default | Options | Description |
|---|---|---|---|---|
| `sortBy` | String | `RECENT` | `RECENT`, `POPULAR` | `RECENT` = newest first; `POPULAR` = most downloaded first |
| `page` | int | `0` | — | Zero-based page index |
| `size` | int | `20` | — | Page size |

#### Example Request

```bash
curl "http://localhost:8082/api/notes/module/CS4135?sortBy=POPULAR&page=0&size=5"
```

#### Response — `200 OK`

Same paginated structure as `GET /my`.

---

### 6. Search Notes by Title

```
GET /api/notes/search?q={query}
```

**Auth required:** No

Case-insensitive title search across all active notes. Uses a `LIKE %query%` match.

#### Query Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `q` | String | Yes | Search term to match against note titles |
| `page` | int | No (default `0`) | Zero-based page index |
| `size` | int | No (default `20`) | Page size |

#### Example Request

```bash
curl "http://localhost:8082/api/notes/search?q=lecture&page=0&size=10"
```

#### Response — `200 OK`

Same paginated structure as `GET /my`.

---

### 7. Delete a Note

```
DELETE /api/notes/{noteId}
```

**Auth required:** Yes

Soft-deletes a note by transitioning its status to `DELETED`. The record is preserved in the database. Only the original uploader may delete a note. Publishes a `NoteDeletedEvent` on success.

#### Path Parameters

| Parameter | Type | Description |
|---|---|---|
| `noteId` | UUID | The note to delete |

#### Example Request

```bash
curl -X DELETE http://localhost:8082/api/notes/3fa85f64-5717-4562-b3fc-2c963f66afa6 \
  -H "Authorization: Bearer <token>"
```

#### Response — `204 No Content`

Empty body.

#### Error Responses

| Status | Reason |
|---|---|
| `403 Forbidden` | Authenticated user is not the uploader of this note |
| `404 Not Found` | Note does not exist |
| `401 Unauthorized` | Missing or invalid JWT |

---

## Error Response Schema

All error responses share the same structure:

```json
{
  "timestamp": "2026-04-14T12:00:00.000000",
  "status":    404,
  "error":     "Not Found",
  "message":   "Note not found with id: 3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

Validation errors include a `fieldErrors` map instead of `message`:

```json
{
  "timestamp":   "2026-04-14T12:00:00.000000",
  "status":      400,
  "error":       "Validation Failed",
  "fieldErrors": {
    "title":      "Title is required",
    "moduleCode": "Module code is required"
  }
}
```

### HTTP Status Code Summary

| Code | Meaning |
|---|---|
| `200 OK` | Successful GET |
| `201 Created` | Note uploaded successfully |
| `204 No Content` | Note deleted successfully |
| `400 Bad Request` | Invalid file type/size or failed validation |
| `401 Unauthorized` | JWT missing or invalid |
| `403 Forbidden` | Valid JWT but insufficient ownership |
| `404 Not Found` | Note does not exist or has been deleted |
| `405 Method Not Allowed` | Wrong HTTP method for endpoint |
| `500 Internal Server Error` | Unexpected server-side failure |

---

## Domain Events Emitted

These events are published asynchronously to RabbitMQ after a successful operation. In local dev mode they are logged instead.

| Event | Trigger Endpoint | Exchange | Routing Key |
|---|---|---|---|
| `NoteUploadedEvent` | `POST /api/notes` | `studyhub.notes` | `note.uploaded` |
| `NoteDownloadedEvent` | `GET /api/notes/{id}/download` | `studyhub.notes` | `note.downloaded` |
| `NoteDeletedEvent` | `DELETE /api/notes/{id}` | `studyhub.notes` | `note.deleted` |

---

## Development Utilities (local profile only)

### Get a Dev Token

```
GET /api/dev/token
```

**Auth required:** No — available in `local` Spring profile only.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `username` | String | `Test User` | Display name embedded in JWT |
| `role` | String | `STUDENT` | Role claim (`STUDENT` or `ADMIN`) |
| `userId` | UUID | auto-generated | Optionally pin the subject UUID |

```bash
curl "http://localhost:8082/api/dev/token?username=abdelrahman&role=STUDENT"
```

```json
{
  "token":    "eyJhbGci...",
  "userId":   "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "username": "abdelrahman",
  "role":     "STUDENT"
}
```

### H2 Database Console

```
GET /h2-console
```

In-browser SQL console for the in-memory H2 database.

| Setting | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:studyhub_notes` |
| Username | `sa` |
| Password | *(empty)* |

### Health Check

```
GET /actuator/health
```

Returns `{"status":"UP"}` when the service is running.
