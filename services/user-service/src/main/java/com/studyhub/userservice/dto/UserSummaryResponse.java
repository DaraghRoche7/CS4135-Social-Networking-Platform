package com.studyhub.userservice.dto;

import com.studyhub.userservice.model.User;

import java.util.UUID;

public class UserSummaryResponse {

    private final UUID userId;
    private final String name;
    private final String email;

    public UserSummaryResponse(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
    }

    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}