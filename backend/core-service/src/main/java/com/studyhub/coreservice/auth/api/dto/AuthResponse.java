package com.studyhub.coreservice.auth.api.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String role
) {
}
