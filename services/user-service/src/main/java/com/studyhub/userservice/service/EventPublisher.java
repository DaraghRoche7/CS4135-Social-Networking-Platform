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
        UserRegisteredEvent event = new UserRegisteredEvent(userId, email, name);
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EVENTS_EXCHANGE, RabbitMQConfig.ROUTING_KEY_REGISTERED, event);
        log.debug("Published UserRegisteredEvent for userId={}", userId);
    }

    public void publishUserFollowed(UUID followerId, UUID followingId) {
        UserFollowedEvent event = new UserFollowedEvent(followerId, followingId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.USER_EVENTS_EXCHANGE, RabbitMQConfig.ROUTING_KEY_FOLLOWED, event);
        log.debug("Published UserFollowedEvent followerId={} followingId={}", followerId, followingId);
    }
}