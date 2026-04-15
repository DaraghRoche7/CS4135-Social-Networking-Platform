package com.studyhub.userservice.exception;

public class NotFollowingException extends RuntimeException {

    public NotFollowingException() {
        super("Not following this user");
    }
}