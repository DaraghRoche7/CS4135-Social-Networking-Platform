package com.studyhub.supportservice.notification.application;

import java.time.Instant;

public record UserAuthenticatedEvent(
    String userId,
    String displayName,
    String email,
    String role,
    Instant occurredAt
) {
}
