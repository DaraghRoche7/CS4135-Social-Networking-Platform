package com.studyhub.userservice.dto;

import com.studyhub.userservice.model.User;

import java.util.UUID;

public record UserSummaryResponse(UUID userId, String name, String email, String role) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
            user.getUserId(), user.getName(), user.getEmail(),
            user.getRole().getName().name()
        );
    }
}