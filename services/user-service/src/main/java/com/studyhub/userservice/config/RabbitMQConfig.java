package com.studyhub.userservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String USER_EVENTS_EXCHANGE = "studyhub.user.events";
    public static final String USER_REGISTERED_QUEUE = "studyhub.user.registered";
    public static final String USER_FOLLOWED_QUEUE = "studyhub.user.followed";
    public static final String ROUTING_KEY_REGISTERED = "user.registered";
    public static final String ROUTING_KEY_FOLLOWED = "user.followed";

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(USER_EVENTS_EXCHANGE, true, false);
    }
    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE).build();
    }

    @Bean
    public Queue userFollowedQueue() {
        return QueueBuilder.durable(USER_FOLLOWED_QUEUE).build();
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(userEventsExchange())
                .with(ROUTING_KEY_REGISTERED);
    }

    @Bean
    public Binding userFollowedBinding() {
        return BindingBuilder.bind(userFollowedQueue())
                .to(userEventsExchange())
                .with(ROUTING_KEY_FOLLOWED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}