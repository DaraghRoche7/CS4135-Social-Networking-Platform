package com.studyhub.supportservice.notification.application;

import com.studyhub.supportservice.notification.api.dto.CreateNotificationRequest;
import com.studyhub.supportservice.notification.api.dto.NotificationListResponse;
import com.studyhub.supportservice.notification.api.dto.NotificationResponse;
import com.studyhub.supportservice.notification.domain.Notification;
import com.studyhub.supportservice.notification.domain.NotificationCategory;
import com.studyhub.supportservice.notification.persistence.NotificationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CoreUserClient coreUserClient;
    private final Clock clock;

    public NotificationService(
        NotificationRepository notificationRepository,
        CoreUserClient coreUserClient,
        Clock clock
    ) {
        this.notificationRepository = notificationRepository;
        this.coreUserClient = coreUserClient;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(String userId, @Nullable Boolean readFilter) {
        List<Notification> notifications = readFilter == null
            ? notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            : notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, readFilter);

        List<NotificationResponse> items = notifications.stream()
            .map(NotificationResponse::from)
            .toList();

        return new NotificationListResponse(items, notificationRepository.countByUserIdAndReadFalse(userId));
    }

    @Transactional
    public NotificationResponse markAsRead(String userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notification.markAsRead(Instant.now(clock));
        return NotificationResponse.from(notification);
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        CoreUserSummaryResponse recipient = coreUserClient.getUserById(request.userId());
        if (!recipient.active()) {
            throw new IllegalArgumentException("Recipient account is inactive");
        }

        Notification notification = notificationRepository.save(new Notification(
            recipient.userId(),
            request.title().trim(),
            request.message().trim(),
            request.category(),
            false,
            clock.instant(),
            null
        ));

        return NotificationResponse.from(notification);
    }

    @Transactional
    public void createLoginNotification(UserAuthenticatedEvent event) {
        notificationRepository.save(new Notification(
            event.userId(),
            "New sign-in recorded",
            "Welcome back, %s. Your StudyHub account was accessed successfully.".formatted(event.displayName()),
            NotificationCategory.SYSTEM,
            false,
            event.occurredAt() != null ? event.occurredAt() : clock.instant(),
            null
        ));
    }
}
