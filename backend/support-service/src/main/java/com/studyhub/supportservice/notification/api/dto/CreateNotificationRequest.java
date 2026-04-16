package com.studyhub.supportservice.notification.api.dto;

import com.studyhub.supportservice.notification.domain.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(
    @NotBlank @Size(max = 100) String userId,
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 1000) String message,
    @NotNull NotificationCategory category
) {
}
