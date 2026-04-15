package com.studyhub.userservice.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class UserRegisteredEvent implements Serializable {

    private final String eventType = "USER_REGISTERED";
    private final UUID userId;
    private final String email;
    private final String name;
    private final Instant timestamp;

    public UserRegisteredEvent(UUID userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.timestamp = Instant.now();
    }

    public String getEventType() { return eventType; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public Instant getTimestamp() { return timestamp; }
}