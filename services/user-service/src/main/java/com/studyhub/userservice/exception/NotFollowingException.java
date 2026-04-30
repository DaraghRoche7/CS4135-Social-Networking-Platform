package com.studyhub.userservice.exception;

public class NotFollowingException extends RuntimeException {
    public NotFollowingException() {
        super("You are not following this user");
    }
}