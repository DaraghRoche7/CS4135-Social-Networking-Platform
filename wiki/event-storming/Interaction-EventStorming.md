# Event Storming — Interaction Context

**Owner:** Nur Alislam Kastiro (22331549)

---

## Session Notes

Mapped out the like/unlike flow to figure out what events we need, what can go wrong, and what the Notification and Feed services need from us.

---

## Event Flow

### Scenario 1: Student likes a note

| Step | Type | Description |
|------|------|-------------|
| 1 | **Actor** | Student (authenticated, JWT in request) |
| 2 | **Command** | `LikePost(postId)` |
| 3 | **Policy** | Extract userId from JWT |
| 4 | **Policy** | Verify post exists — call Notes service (ACL). If down: circuit breaker fallback assumes post exists and proceeds |
| 5 | **Policy** | Check (postId, userId) not already in Likes table |
| 6 | **Event** | `PostLikedEvent { likeId, postId, userId, timestamp }` |
| 7 | **Read Model** | Like count for post incremented |
| 8 | **Downstream** | Notification service consumes event → notifies post author |

**Hotspot:** What if two requests arrive simultaneously for the same user + post?
- Resolution: DB unique constraint on `(post_id, user_id)` acts as the final safety net. One request gets a 409, the other succeeds.

---

### Scenario 2: Student unlikes a note

| Step | Type | Description |
|------|------|-------------|
| 1 | **Actor** | Student |
| 2 | **Command** | `UnlikePost(postId)` |
| 3 | **Policy** | Extract userId from JWT |
| 4 | **Policy** | Check like record exists — if not, 404 |
| 5 | **Event** | `PostUnlikedEvent { postId, userId, timestamp }` |
| 6 | **Read Model** | Like count decremented |

---

### Scenario 3: Student views like count on a note

| Step | Type | Description |
|------|------|-------------|
| 1 | **Actor** | Student (or anonymous if we allow it later) |
| 2 | **Command** | `GetLikeInfo(postId)` |
| 3 | **Query** | `COUNT(*) WHERE post_id = ?` + `EXISTS WHERE post_id = ? AND user_id = ?` |
| 4 | **Response** | `{ postId, likeCount, likedByCurrentUser }` |

No event published — pure query, no side effects.

---

## Commands

| Command | Triggered By | Handler |
|---------|-------------|---------|
| LikePost | Student clicks Like button | LikeController → LikeService |
| UnlikePost | Student clicks Unlike button | LikeController → LikeService |
| GetLikeInfo | Feed rendering, post view | LikeController → LikeService |

---

## Domain Events

| Event | Published By | Consumed By |
|-------|-------------|-------------|
| PostLikedEvent | Interaction service (RabbitMQ) | Notification service |
| PostUnlikedEvent | Interaction service (RabbitMQ) | Notification service (optional cleanup) |

Exchange: `studyhub.events`
Routing keys: `interaction.post.liked`, `interaction.post.unliked`

---

## Policies

- A user cannot like a post they already liked → 409 Conflict
- A user cannot unlike a post they haven't liked → 404 Not Found
- Post must exist before a like can be recorded → verified via Notes service ACL, circuit-broken
- `userId` is always sourced from the validated JWT — never from the request body

---

## Conflicts & Uncertainties (Hotspots)

| Hotspot | Resolution |
|---------|-----------|
| Notes service is down during LikePost | Resilience4j circuit breaker → fallback assumes post exists and proceeds (availability over strict consistency) |
| Concurrent duplicate like requests | DB unique constraint catches the second one |
| Should like count be cached? | Out of scope for this context — Feed service can cache aggregate counts if needed |
| Should we expose who liked a post (list of users)? | Not required for MVP — only count + likedByMe flag needed |

---

## Domain Story: Liking a Note

> Sarah is a second-year CS student. She opens her StudyHub feed and sees a set of revision notes for CS4135 uploaded by a classmate. She clicks Like. The system checks she hasn't already liked it, records the interaction, and sends a notification to the note's author. The like count on the note goes up by one. Later, Sarah changes her mind and clicks Unlike — the like is removed and the count goes back down.

This covers the main like/unlike flow end to end.
