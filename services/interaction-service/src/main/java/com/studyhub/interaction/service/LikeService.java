package com.studyhub.interaction.service;

import com.studyhub.interaction.client.NotesServiceClient;
import com.studyhub.interaction.domain.Like;
import com.studyhub.interaction.dto.LikeCountResponse;
import com.studyhub.interaction.dto.LikeResponse;
import com.studyhub.interaction.events.PostLikedEvent;
import com.studyhub.interaction.events.PostUnlikedEvent;
import com.studyhub.interaction.exception.DuplicateLikeException;
import com.studyhub.interaction.exception.LikeNotFoundException;
import com.studyhub.interaction.exception.PostNotFoundException;
import com.studyhub.interaction.messaging.LikeEventPublisher;
import com.studyhub.interaction.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final LikeEventPublisher eventPublisher;
    private final NotesServiceClient notesServiceClient;

    @Transactional
    public LikeResponse likePost(UUID postId, UUID userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new DuplicateLikeException("You have already liked this post");
        }

        if (!notesServiceClient.postExists(postId)) {
            throw new PostNotFoundException("Post not found: " + postId);
        }

        Like like = Like.builder()
                .postId(postId)
                .userId(userId)
                .build();

        Like saved = likeRepository.save(like);
        log.debug("Like saved: likeId={}, postId={}, userId={}", saved.getLikeId(), postId, userId);

        eventPublisher.publishPostLiked(new PostLikedEvent(
                saved.getLikeId(),
                postId,
                userId,
                LocalDateTime.now()
        ));

        return new LikeResponse(saved.getLikeId(), saved.getPostId(), saved.getUserId(), saved.getCreatedAt());
    }

    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new LikeNotFoundException("You have not liked this post");
        }

        likeRepository.deleteByPostIdAndUserId(postId, userId);
        log.debug("Like removed: postId={}, userId={}", postId, userId);

        eventPublisher.publishPostUnliked(new PostUnlikedEvent(postId, userId, LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public LikeCountResponse getLikeInfo(UUID postId, UUID userId) {
        long count = likeRepository.countByPostId(postId);
        boolean liked = userId != null && likeRepository.existsByPostIdAndUserId(postId, userId);
        return new LikeCountResponse(postId, count, liked);
    }
}
