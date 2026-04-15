package com.studyhub.coreservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    @ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
    TopicExchange studyHubExchange(AppProperties appProperties) {
        return new TopicExchange(appProperties.getMessaging().getExchange(), true, false);
    }

    @Bean
    @ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
    Binding coreLoginBinding(TopicExchange studyHubExchange, AppProperties appProperties) {
        return BindingBuilder.bind(org.springframework.amqp.core.QueueBuilder.durable("studyhub.auth.audit").build())
            .to(studyHubExchange)
            .with(appProperties.getMessaging().getLoginRoutingKey());
    }

    @Bean
    MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
