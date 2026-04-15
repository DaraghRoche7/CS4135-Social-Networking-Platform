package com.studyhub.noteservice.exception;

/**
 * Thrown when a user attempts an action they are not authorized to perform,
 * such as deleting a note they do not own.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
