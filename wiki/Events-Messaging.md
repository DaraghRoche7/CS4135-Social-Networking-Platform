# Events & Messaging (Planned)

## Overview

This document describes the planned event-driven architecture and messaging patterns.

## Message Broker

### Technology
- **RabbitMQ** for asynchronous message queuing
- Event-driven communication between services
- Decoupled service interactions

## Planned Events

### Post Events
- **PostCreated**: Published when a new post is created
  - Triggers: Feed generation, notifications to followers
- **PostUpdated**: Published when a post is updated
  - Triggers: Feed cache invalidation
- **PostDeleted**: Published when a post is deleted
  - Triggers: Feed cache invalidation, notification cleanup

### User Events
- **UserFollowed**: Published when a user follows another user
  - Triggers: Feed subscription updates
- **UserUnfollowed**: Published when a user unfollows another user
  - Triggers: Feed subscription cleanup
- **UserRegistered**: Published when a new user registers
  - Triggers: Welcome notification, initial feed setup

### Interaction Events
- **PostLiked**: Published when a post is liked
  - Triggers: Notification to post author
- **PostUnliked**: Published when a post is unliked
  - Triggers: Notification cleanup (if applicable)

## Event Flow

### Post Creation Flow
1. User creates post via Core Service
2. Core Service validates and stores post
3. Core Service publishes `PostCreated` event to RabbitMQ
4. Support Service consumes `PostCreated` event
5. Support Service fans out post to all followers' feeds
6. Support Service caches feeds in Redis
7. Support Service sends notifications to followers

### Fan-Out Pattern
- When a post is created, it's added to all followers' feeds
- Feeds are cached in Redis for fast retrieval
- Cache invalidation on post updates/deletes

## Message Structure (Planned)

Events will contain:
- Event type
- Timestamp
- User ID (who triggered the event)
- Relevant data (post ID, target user ID, etc.)

---

**Status:** Planned - Event structure and routing will be defined during implementation.

