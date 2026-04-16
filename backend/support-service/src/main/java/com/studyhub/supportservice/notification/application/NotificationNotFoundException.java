package com.studyhub.supportservice.notification.application;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(Long notificationId) {
        super("Notification %d was not found".formatted(notificationId));
    }
}
