package ie.ul.studyhub.support.service.dto;

import java.time.Instant;

public record FeedItemDto(
    Long id,
    String title,
    String description,
    String moduleCode,
    String uploaderUserId,
    Instant createdAt,
    long likesCount,
    boolean likedByUser) {}

