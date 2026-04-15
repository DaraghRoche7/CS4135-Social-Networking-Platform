package com.studyhub.userservice.exception;

public class AlreadyFollowingException extends RuntimeException {

    public AlreadyFollowingException() {
        super("Already following this user");
    }
}