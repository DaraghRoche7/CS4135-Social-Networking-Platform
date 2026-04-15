package com.studyhub.noteservice.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Development-only controller that issues test JWT tokens.
 * Only active in the "local" Spring profile — never deployed to production.
 */
@RestController
@RequestMapping("/api/dev")
@Profile("local")
public class DevAuthController {

    private final SecretKey secretKey;
    private final long expirationMs;

    public DevAuthController(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Returns a signed JWT for the given user details (or defaults to a test user).
     */
    @GetMapping("/token")
    public Map<String, String> getToken(
            @RequestParam(defaultValue = "") String userId,
            @RequestParam(defaultValue = "Test User") String username,
            @RequestParam(defaultValue = "STUDENT") String role) {

        String uid = userId.isBlank() ? UUID.randomUUID().toString() : userId;

        String token = Jwts.builder()
                .subject(uid)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();

        return Map.of("token", token, "userId", uid, "username", username, "role", role);
    }
}
