package com.studyhub.interaction.messaging;

import com.studyhub.interaction.events.PostLikedEvent;
import com.studyhub.interaction.events.PostUnlikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public static final String EXCHANGE = "studyhub.events";
    public static final String LIKED_ROUTING_KEY = "interaction.post.liked";
    public static final String UNLIKED_ROUTING_KEY = "interaction.post.unliked";

    public void publishPostLiked(PostLikedEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, LIKED_ROUTING_KEY, event);
            log.debug("Published PostLikedEvent for postId={}", event.getPostId());
        } catch (Exception e) {
            log.error("Failed to publish PostLikedEvent for postId={}: {}", event.getPostId(), e.getMessage());
        }
    }

    public void publishPostUnliked(PostUnlikedEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, UNLIKED_ROUTING_KEY, event);
            log.debug("Published PostUnlikedEvent for postId={}", event.getPostId());
        } catch (Exception e) {
            log.error("Failed to publish PostUnlikedEvent for postId={}: {}", event.getPostId(), e.getMessage());
        }
    }
}
