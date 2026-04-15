package com.studyhub.coreservice.auth.application;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.config.AppProperties;
import java.time.Clock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class RabbitAuthenticationEventPublisher implements AuthenticationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;
    private final Clock clock;

    public RabbitAuthenticationEventPublisher(
        RabbitTemplate rabbitTemplate,
        AppProperties appProperties,
        Clock clock
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.appProperties = appProperties;
        this.clock = clock;
    }

    @Override
    public void publishLoginSuccess(StudyHubUser user) {
        String primaryRole = user.getRoles().stream()
            .map(role -> role.getCode())
            .sorted()
            .findFirst()
            .orElse("USER");

        rabbitTemplate.convertAndSend(
            appProperties.getMessaging().getExchange(),
            appProperties.getMessaging().getLoginRoutingKey(),
            new UserAuthenticatedEvent(
                user.getPublicId(),
                user.getDisplayName(),
                user.getEmail(),
                primaryRole,
                clock.instant()
            )
        );
    }
}
