package com.studyhub.supportservice.notification.application;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class UserAuthenticationEventListener {

    private final NotificationService notificationService;

    public UserAuthenticationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.messaging.login-queue}")
    public void handleUserAuthenticated(UserAuthenticatedEvent event) {
        notificationService.createLoginNotification(event);
    }
}
