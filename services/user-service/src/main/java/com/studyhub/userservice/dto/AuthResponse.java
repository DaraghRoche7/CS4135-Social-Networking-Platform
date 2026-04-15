package com.studyhub.userservice.dto;

import java.util.UUID;

public class AuthResponse {

    private final String token;
    private final UUID userId;
    private final String name;
    private final String email;
    private final String role;

    public AuthResponse(String token, UUID userId, String name, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}