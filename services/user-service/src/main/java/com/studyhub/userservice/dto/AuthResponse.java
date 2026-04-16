package com.studyhub.userservice.dto;

import java.util.UUID;

public class AuthResponse {

    private final String token;
    private final String refreshToken;
    private final UUID userId;
    private final String name;
    private final String email;
    private final String role;

    public AuthResponse(String token, String refreshToken, UUID userId, String name, String email, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
