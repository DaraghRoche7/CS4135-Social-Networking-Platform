package com.studyhub.userservice.event;

import java.util.UUID;

public record UserFollowedEvent(UUID followerId, UUID followingId) {}