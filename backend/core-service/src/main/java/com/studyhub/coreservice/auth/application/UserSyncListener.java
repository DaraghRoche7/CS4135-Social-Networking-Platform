package com.studyhub.coreservice.auth.application;

import com.studyhub.coreservice.auth.domain.Role;
import com.studyhub.coreservice.auth.domain.StudyHubUser;
import com.studyhub.coreservice.auth.persistence.RoleRepository;
import com.studyhub.coreservice.auth.persistence.StudyHubUserRepository;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.messaging.enabled", havingValue = "true")
public class UserSyncListener {

    private static final Logger log = LoggerFactory.getLogger(UserSyncListener.class);

    private final StudyHubUserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserSyncListener(StudyHubUserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @RabbitListener(queues = "studyhub.user.sync")
    @Transactional
    public void onUserRegistered(UserRegisteredEvent event) {
        if (userRepository.existsByEmailIgnoreCase(event.email())) {
            log.debug("User {} already exists in core-service, skipping sync", event.email());
            return;
        }

        Role userRole = roleRepository.findByCode("USER")
            .orElseGet(() -> roleRepository.save(new Role("USER", "Student account access")));

        StudyHubUser user = new StudyHubUser(
            event.email(),
            event.email(),
            event.name() != null ? event.name() : event.email(),
            "{noop}EXTERNAL",
            true,
            Instant.now(),
            Set.of(userRole)
        );
        userRepository.save(user);
        log.info("Synced registered user {} into core-service", event.email());
    }
}