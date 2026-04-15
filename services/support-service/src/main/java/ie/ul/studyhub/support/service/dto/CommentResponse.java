package ie.ul.studyhub.support.service.dto;

import java.time.Instant;

public record CommentResponse(long id, String userId, String body, Instant createdAt) {}
