# Events & Messaging

## Overview

This document describes the messaging infrastructure available in the prototype and how it is intended to be used. Not all event-driven integrations from the domain design are implemented end-to-end in the prototype.

## Message Broker

### Technology
- **RabbitMQ** for asynchronous message queuing
- Event-driven communication between services
- Decoupled service interactions

## Events (prototype + intended future work)

### Post / Feed events
- Prototype note:
  - Feed reads are served from PostgreSQL via core-service repository queries.
  - Redis is provisioned but not currently used for feed caching.
- Future work:
  - PostCreated/PostUpdated/PostDeleted events can be introduced to support cache invalidation and downstream notifications.

### User Events
- **UserRegistered**: published by user-service when a new user registers (prototype wiring present)
- **UserFollowed**: published by user-service when a user follows another user (prototype wiring present)

Note: not all consumers are implemented; the publishing service has no knowledge of its consumers (bounded-context decoupling).

### Interaction Events
- Prototype note:
  - Likes/comments are implemented inside core-service in the current prototype.
- Future work:
  - PostLiked/PostUnliked events can be published to notify authors or drive analytics.

## Event Flow (prototype reality)

### Post / Feed flow (today)
1. User creates post via core-service (through API gateway)
2. core-service validates and stores post in PostgreSQL
3. Feed reads query PostgreSQL and return results directly (no Redis cache-aside)

### Fan-out pattern (future work)
- Introduce a fan-out flow if the project is extended:
  - publish events on writes (post create/engagement)
  - consume to precompute feeds or invalidate caches
  - measure whether the complexity is justified for the expected scale

## Message Structure (Planned)

Events will contain:
- Event type
- Timestamp
- User ID (who triggered the event)
- Relevant data (post ID, target user ID, etc.)

## Support Service dependency note (resilience)
- Support-service calls core-service internal endpoints for validation (synchronous HTTP).
- Outbound calls are protected with connect/read timeouts and bounded retries to avoid stalling consumers if core-service is slow/unavailable.

