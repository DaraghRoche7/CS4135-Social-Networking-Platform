package com.studyhub.supportservice.notification.api.dto;

import com.studyhub.supportservice.notification.domain.Notification;
import java.time.Instant;

public record NotificationResponse(
    Long id,
    String userId,
    String title,
    String message,
    String category,
    Instant createdAt,
    boolean read
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getUserId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getCategory().getDisplayName(),
            notification.getCreatedAt(),
            notification.isRead());
    }
}
