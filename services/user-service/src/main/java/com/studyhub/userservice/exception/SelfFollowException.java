package com.studyhub.userservice.exception;

public class SelfFollowException extends RuntimeException {
    public SelfFollowException() {
        super("You cannot follow yourself");
    }
}