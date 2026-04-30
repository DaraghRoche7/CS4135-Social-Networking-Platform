package com.studyhub.coreservice.auth.application;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String email, String name) {}