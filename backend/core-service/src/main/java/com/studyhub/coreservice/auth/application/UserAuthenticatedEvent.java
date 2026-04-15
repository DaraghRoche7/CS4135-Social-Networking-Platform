package com.studyhub.coreservice.auth.application;

import java.time.Instant;

public record UserAuthenticatedEvent(
    String userId,
    String displayName,
    String email,
    String role,
    Instant occurredAt
) {
}
