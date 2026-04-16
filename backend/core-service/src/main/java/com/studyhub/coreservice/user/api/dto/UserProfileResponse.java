package com.studyhub.coreservice.user.api.dto;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import java.util.Comparator;
import java.util.Set;

public record UserProfileResponse(
    String userId,
    String email,
    String displayName,
    Set<String> roles,
    long followersCount,
    long followingCount
) {

    public static UserProfileResponse from(StudyHubUser user, long followersCount, long followingCount) {
        return new UserProfileResponse(
            user.getPublicId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getRoles().stream()
                .map(role -> role.getCode())
                .sorted(Comparator.naturalOrder())
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)),
            followersCount,
            followingCount
        );
    }
}
