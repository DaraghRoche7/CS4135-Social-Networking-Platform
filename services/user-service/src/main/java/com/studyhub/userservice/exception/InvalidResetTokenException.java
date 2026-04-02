package com.studyhub.userservice.exception;

public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("Password reset token is invalid or has expired");
    }
}