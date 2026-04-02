package com.studyhub.userservice.dto;

import com.studyhub.userservice.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileResponse {

    private final UUID userId;
    private final String name;
    private final String email;
    private final String role;
    private final String course;
    private final Integer year;
    private final String modules;
    private final LocalDateTime createdAt;

    public UserProfileResponse(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().getName().name();
        this.course = user.getCourse();
        this.year = user.getYear();
        this.modules = user.getModules();
        this.createdAt = user.getCreatedAt();
    }

    public UUID getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getCourse() { return course; }
    public Integer getYear() { return year; }
    public String getModules() { return modules; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}