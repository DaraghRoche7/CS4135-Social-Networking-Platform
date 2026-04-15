# Note Management — Aggregate Design

## Overview

The Notes bounded context contains a single aggregate: the **Note Aggregate**. All domain logic for uploading, downloading, and managing study notes flows through this aggregate root.

---

## Note Aggregate

### Aggregate Root

`Note` (`domain/entity/Note.java`) is the aggregate root. Every operation that changes note state must go through this entity — nothing else in the bounded context bypasses it.

### Members

| Member | Type | Role |
|---|---|---|
| `Note` | Entity (root) | Identity, lifecycle, file metadata, ownership |
| `NoteStatus` | Value Object (enum) | Immutable status: `ACTIVE`, `DELETED`, `FLAGGED` |
| `storagePath` | Value Object (embedded String) | Immutable reference to the physical file location set once at upload |

> **Note on design:** `NoteMetadata` and `NoteFile` are not modelled as separate Java classes — their fields are flattened directly into `Note` to keep persistence simple (single table, no joins). Conceptually they form value objects; structurally they are inline fields. This is a deliberate implementation trade-off, not a DDD violation.

### Fields on the Root

```
id            UUID          — surrogate identity, generated at persist time
uploaderId    UUID          — reference to User aggregate (foreign key, not object)
title         String        — human-readable name for the note
description   String        — optional extended description
moduleCode    String        — academic module this note belongs to (e.g. CS4135)
moduleName    String        — human-readable module name
fileName      String        — original filename as uploaded
fileSize      Long          — size in bytes (used for display and validation)
mimeType      String        — must be application/pdf
storagePath   String        — absolute path on disk (set once, never changed)
downloadCount Long          — monotonically increasing, updated on each download
status        NoteStatus    — ACTIVE | DELETED | FLAGGED (lifecycle state machine)
createdAt     LocalDateTime — set by @PrePersist, immutable thereafter
updatedAt     LocalDateTime — updated by @PreUpdate and all mutating methods
```

### Business Methods (Invariants Enforced)

```java
incrementDownloadCount()   // Increments counter; bumps updatedAt
markAsDeleted()            // ACTIVE/FLAGGED → DELETED; soft delete only
flag()                     // ACTIVE → FLAGGED; triggers admin review
isOwnedBy(UUID userId)     // Returns true if userId == uploaderId
```

All status transitions are guarded — external callers cannot set `status` directly to an arbitrary value; they call the named methods which encode the allowed transitions.

---

## Boundary Rationale

### What is inside the boundary

**File metadata** (fileName, fileSize, mimeType, storagePath) is co-located with Note because:
- A note cannot exist without its file — they are created atomically in a single transaction.
- Deleting a note logically deletes the file reference; there is no scenario where the metadata exists without the file or vice versa.
- Consistency is trivial: one row in one table.

**Download counter** is inside because:
- It must be updated in the same transaction as serving the file to prevent race conditions.
- It belongs to the note's identity — two notes with identical content but different download counts are meaningfully different.

**NoteStatus** is inside because:
- Status transitions (`markAsDeleted`, `flag`) are invariants that only make sense in the context of the whole note.
- Querying by status is always scoped to a specific note, not across aggregates.

### What is outside the boundary

| Concept | Why excluded |
|---|---|
| **User / Uploader** | Separate bounded context (User/Auth service). Referenced only by `uploaderId` UUID — no object reference, no join. Cross-context calls go through JWT claims, not domain objects. |
| **Likes** | Owned by the Likes bounded context (Paudie Kelly). The Notes service publishes `NoteUploadedEvent`; the Likes service subscribes and manages its own aggregate. Eventual consistency is acceptable — a like count being briefly stale does not violate any Note invariant. |
| **Comments** | Not yet implemented; would be a separate aggregate with its own consistency boundary. Comments can be eventually consistent relative to the Note they reference. |
| **Module** | A module is a reference data concept that spans multiple bounded contexts. It is represented here as a plain `String moduleCode` — no foreign key to a Module aggregate. |
| **Feed / Notifications** | Downstream consumers of domain events. They react to `NoteUploaded` / `NoteDeleted` events but have no authority over Note state. |

---

## Invariants Summary

| # | Invariant | Where enforced |
|---|---|---|
| 1 | Only PDF files (MIME `application/pdf`, `.pdf` extension, ≤ 10 MB) may be uploaded | `NoteServiceImpl.validateFile()` |
| 2 | A note must have a title and module code | `@Column(nullable = false)` + request validation |
| 3 | A note's `storagePath` is set once at creation and never changed | No setter called after `Note.builder().storagePath(...).build()` |
| 4 | Only the uploader may delete their note | `note.isOwnedBy(requesterId)` check in `NoteServiceImpl.deleteNote()` |
| 5 | Deletion is soft — notes transition to `DELETED`, never removed from the DB | `note.markAsDeleted()` sets status; no `DELETE` SQL |
| 6 | Download count only increases | `incrementDownloadCount()` has no decrement counterpart |
| 7 | Status can only be changed via named methods, not direct assignment | Business methods are the only mutation path exposed publicly |

---

## Repository Contract

`NoteRepository` is the **only** mechanism for persisting or retrieving Note aggregates.

```java
// Persistence
Note save(Note note)

// Retrieval — always filtered by NoteStatus.ACTIVE unless explicitly overridden
Optional<Note> findByIdAndStatus(UUID id, NoteStatus status)
Page<Note>     findByUploaderIdAndStatus(UUID uploaderId, NoteStatus status, Pageable p)
Page<Note>     findByModuleCodeAndStatus(String moduleCode, NoteStatus status, Pageable p)
Page<Note>     findByModuleCodeAndStatusOrderByDownloadCountDesc(String code, NoteStatus status, Pageable p)
Page<Note>     searchByTitleContainingIgnoreCaseAndStatus(String title, NoteStatus status, Pageable p)
Page<Note>     findByStatus(NoteStatus status, Pageable p)
```

Filtering by `status` in every query ensures deleted and flagged notes are never accidentally surfaced. All queries go through the repository — the service layer never constructs raw JPQL or uses the entity manager directly.

---

## Domain Events Published

Events are published **after** the aggregate is saved, ensuring they only fire on successful commits. All publishing uses Resilience4j circuit breakers — if RabbitMQ is unavailable, the event is logged and the operation still succeeds (eventual consistency, not strong consistency, is required for downstream consumers).

| Event | Trigger | Consumers (cross-context) |
|---|---|---|
| `NoteUploadedEvent` | Successful `uploadNote()` | Feed service, Notifications service |
| `NoteDownloadedEvent` | Successful `downloadNote()` | Feed service (activity tracking) |
| `NoteDeletedEvent` | Successful `deleteNote()` | Feed service, Notifications service |

---

## Aggregate Lifecycle Diagram

```
                    ┌─────────────────────────────┐
                    │        NoteRepository        │
                    │  (only persistence gateway)  │
                    └────────────┬────────────────┘
                                 │ save / find
                                 ▼
                    ┌────────────────────────┐
                    │     Note (root)        │
                    │  status: ACTIVE        │──────┐ markAsDeleted()
                    │  downloadCount: 0      │      ▼
                    │  storagePath: /...     │   DELETED
                    └────────┬───────────────┘      
                             │ flag()               
                             ▼                      
                          FLAGGED                   

  On creation: file validated → stored to disk → Note built → saved → event published
  On download: Note loaded → downloadCount++ → saved → event published → file served
  On delete:   Note loaded → isOwnedBy() → markAsDeleted() → saved → event published
```
