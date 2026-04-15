package com.studyhub.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LikeResponse {

    private UUID likeId;
    private UUID postId;
    private UUID userId;
    private LocalDateTime createdAt;
}
