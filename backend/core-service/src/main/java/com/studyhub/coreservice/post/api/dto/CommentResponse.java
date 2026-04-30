package com.studyhub.coreservice.post.api.dto;

import com.studyhub.coreservice.post.domain.PostComment;
import java.time.Instant;

public record CommentResponse(
        Long id,
        Long postId,
        String userId,
        String displayName,
        String body,
        Instant createdAt
) {

    public static CommentResponse from(PostComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getUser().getPublicId(),
                comment.getUser().getDisplayName(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }
}