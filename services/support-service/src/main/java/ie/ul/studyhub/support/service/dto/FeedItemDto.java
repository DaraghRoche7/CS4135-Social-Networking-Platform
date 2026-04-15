package ie.ul.studyhub.support.service.dto;

import java.time.Instant;

public record FeedItemDto(
    Long id,
    String title,
    String description,
    String module,
    Long uploaderUserId,
    Instant createdAt,
    long likeCount,
    boolean interactedByUser) {}

