package com.studyhub.interaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LikeCountResponse {

    private UUID postId;
    private long likeCount;
    private boolean likedByCurrentUser;
}
