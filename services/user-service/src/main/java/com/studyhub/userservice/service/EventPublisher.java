package com.studyhub.userservice.service;

import com.studyhub.userservice.config.RabbitMQConfig;
import com.studyhub.userservice.event.UserFollowedEvent;
import com.studyhub.userservice.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishUserRegistered(UUID userId, String email, String name) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "user.registered",
                new UserRegisteredEvent(userId, email, name));
        } catch (Exception e) {
            log.warn("Failed to publish user.registered event for {}: {}", email, e.getMessage());
        }
    }

    public void publishUserFollowed(UUID followerId, UUID followingId) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "user.followed",
                new UserFollowedEvent(followerId, followingId));
        } catch (Exception e) {
            log.warn("Failed to publish user.followed event: {}", e.getMessage());
        }
    }
}