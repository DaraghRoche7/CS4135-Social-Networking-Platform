package com.studyhub.supportservice.notification.application;

import java.util.Set;

public record CoreUserSummaryResponse(
    String userId,
    String email,
    String displayName,
    boolean active,
    Set<String> roles
) {
}
