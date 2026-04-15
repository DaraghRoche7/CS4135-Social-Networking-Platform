package com.studyhub.supportservice.notification.application;

public class RecipientNotFoundException extends RuntimeException {

    public RecipientNotFoundException(String userId) {
        super("Recipient %s was not found".formatted(userId));
    }
}
