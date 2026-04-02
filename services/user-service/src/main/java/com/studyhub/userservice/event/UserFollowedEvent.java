package com.studyhub.userservice.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class UserFollowedEvent implements Serializable {

    private final String eventType = "USER_FOLLOWED";
    private final UUID userId;
    private final UUID targetUserId;
    private final Instant timestamp;

    public UserFollowedEvent(UUID userId, UUID targetUserId) {
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.timestamp = Instant.now();
    }

    public String getEventType() { return eventType; }
    public UUID getUserId() { return userId; }
    public UUID getTargetUserId() { return targetUserId; }
    public Instant getTimestamp() { return timestamp; }
}