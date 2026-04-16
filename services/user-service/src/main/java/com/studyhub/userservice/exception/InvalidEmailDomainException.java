package com.studyhub.userservice.exception;

public class InvalidEmailDomainException extends RuntimeException {

    public InvalidEmailDomainException(String email) {
        super("Email '" + email + "' is not allowed. Only @ul.ie and @studentmail.ul.ie addresses are accepted.");
    }
}