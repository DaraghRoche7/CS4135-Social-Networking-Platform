package com.studyhub.coreservice.auth.application;

import com.studyhub.coreservice.auth.domain.StudyHubUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpAuthenticationEventPublisher implements AuthenticationEventPublisher {

    @Override
    public void publishLoginSuccess(StudyHubUser user) {
        // Messaging is optional in local test runs; no-op keeps the login flow available.
    }
}
