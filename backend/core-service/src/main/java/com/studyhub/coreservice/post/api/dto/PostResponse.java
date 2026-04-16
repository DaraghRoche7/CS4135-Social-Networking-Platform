package com.studyhub.coreservice.post.api.dto;

import com.studyhub.coreservice.post.domain.Post;
import java.time.Instant;

public record PostResponse(
    Long id,
    String title,
    String description,
    String module,
    String authorUserId,
    String authorDisplayName,
    String originalFileName,
    String contentType,
    long fileSize,
    long likesCount,
    boolean likedByUser,
    Instant createdAt,
    Instant updatedAt,
    String fileUrl
) {

    public static PostResponse from(Post post, long likesCount, boolean likedByUser) {
        return new PostResponse(
            post.getId(),
            post.getTitle(),
            post.getDescription(),
            post.getModuleCode(),
            post.getAuthor().getPublicId(),
            post.getAuthor().getDisplayName(),
            post.getOriginalFileName(),
            post.getContentType(),
            post.getFileSize(),
            likesCount,
            likedByUser,
            post.getCreatedAt(),
            post.getUpdatedAt(),
            "/api/posts/%s/file".formatted(post.getId())
        );
    }
}
