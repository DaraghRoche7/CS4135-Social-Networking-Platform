package com.studyhub.supportservice.notification.domain;

public enum NotificationCategory {
    COURSE("Course"),
    SOCIAL("Social"),
    DIGEST("Digest"),
    SYSTEM("System"),
    MODERATION("Moderation");

    private final String displayName;

    NotificationCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
