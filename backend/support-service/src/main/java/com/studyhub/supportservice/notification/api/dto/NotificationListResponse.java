package com.studyhub.supportservice.notification.api.dto;

import java.util.List;

public record NotificationListResponse(
    List<NotificationResponse> items,
    long unreadCount
) {
}
