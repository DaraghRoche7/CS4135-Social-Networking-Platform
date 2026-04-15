package com.studyhub.userservice.exception;

public class SelfFollowException extends RuntimeException {

    public SelfFollowException() {
        super("A user cannot follow themselves");
    }
}