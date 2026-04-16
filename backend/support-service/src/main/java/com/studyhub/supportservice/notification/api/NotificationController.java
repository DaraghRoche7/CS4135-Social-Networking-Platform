package com.studyhub.supportservice.notification.api;

import com.studyhub.supportservice.notification.api.dto.CreateNotificationRequest;
import com.studyhub.supportservice.notification.api.dto.NotificationListResponse;
import com.studyhub.supportservice.notification.api.dto.NotificationResponse;
import com.studyhub.supportservice.notification.application.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Notification APIs for authenticated users")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get notifications for the authenticated user")
    public NotificationListResponse getNotifications(
        Principal principal,
        @RequestParam(required = false) Boolean read
    ) {
        return notificationService.getNotifications(principal.getName(), read);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a notification for a target user")
    public NotificationResponse createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.createNotification(request);
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Mark a notification as read")
    public NotificationResponse markAsRead(
        Principal principal,
        @PathVariable Long notificationId
    ) {
        return notificationService.markAsRead(principal.getName(), notificationId);
    }
}
