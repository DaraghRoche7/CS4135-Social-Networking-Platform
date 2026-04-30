package com.studyhub.userservice.dto;

import com.studyhub.userservice.model.User;

import java.util.UUID;

public record UserProfileResponse(UUID userId, String name, String email, String course, Integer year, String modules, String role, long followerCount, long followingCount) {
    public static UserProfileResponse from(User user, long followerCount, long followingCount) {
        return new UserProfileResponse(
            user.getUserId(), user.getName(), user.getEmail(),
            user.getCourse(), user.getYear(), user.getModules(),
            user.getRole().getName().name(), followerCount, followingCount
        );
    }
}