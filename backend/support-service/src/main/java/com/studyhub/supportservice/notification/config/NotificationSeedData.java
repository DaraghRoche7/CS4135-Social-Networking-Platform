package com.studyhub.supportservice.notification.config;

import com.studyhub.supportservice.notification.domain.Notification;
import com.studyhub.supportservice.notification.domain.NotificationCategory;
import com.studyhub.supportservice.notification.persistence.NotificationRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
public class NotificationSeedData {

    @Bean
    ApplicationRunner seedNotifications(NotificationRepository notificationRepository) {
        return args -> {
            if (notificationRepository.count() > 0) {
                return;
            }

            notificationRepository.saveAll(List.of(
                new Notification(
                    "demo-user",
                    "Assignment deadline moved",
                    "CS4135 project deadline has moved to Friday 5 PM.",
                    NotificationCategory.COURSE,
                    false,
                    Instant.parse("2026-04-15T12:15:00Z"),
                    null
                ),
                new Notification(
                    "demo-user",
                    "New follower request",
                    "A classmate wants to connect with you.",
                    NotificationCategory.SOCIAL,
                    false,
                    Instant.parse("2026-04-15T11:00:00Z"),
                    null
                ),
                new Notification(
                    "demo-user",
                    "Weekly digest",
                    "Your study group posted 4 new notes this week.",
                    NotificationCategory.DIGEST,
                    true,
                    Instant.parse("2026-04-14T18:30:00Z"),
                    Instant.parse("2026-04-14T19:00:00Z")
                ),
                new Notification(
                    "admin-user",
                    "Moderation queue updated",
                    "Two new reports were added to the moderation queue.",
                    NotificationCategory.MODERATION,
                    false,
                    Instant.parse("2026-04-15T09:30:00Z"),
                    null
                )
            ));
        };
    }
}
