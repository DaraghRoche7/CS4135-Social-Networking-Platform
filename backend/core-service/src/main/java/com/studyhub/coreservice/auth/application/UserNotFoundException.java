package com.studyhub.coreservice.auth.application;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User %s was not found".formatted(userId));
    }
}
