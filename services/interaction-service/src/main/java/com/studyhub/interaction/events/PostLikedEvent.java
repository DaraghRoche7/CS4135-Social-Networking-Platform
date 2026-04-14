package com.studyhub.interaction.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLikedEvent {

    private UUID likeId;
    private UUID postId;
    private UUID userId;
    private LocalDateTime timestamp;
}
