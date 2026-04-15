package com.studyhub.coreservice.auth.api.dto;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import java.util.Set;

public record UserSummaryResponse(
    String userId,
    String email,
    String displayName,
    boolean active,
    Set<String> roles
) {

    public static UserSummaryResponse from(StudyHubUser user) {
        return new UserSummaryResponse(
            user.getPublicId(),
            user.getEmail(),
            user.getDisplayName(),
            user.isActive(),
            user.getRoles().stream()
                .map(role -> role.getCode())
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new))
        );
    }
}
