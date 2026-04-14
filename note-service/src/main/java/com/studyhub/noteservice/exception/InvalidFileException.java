package com.studyhub.noteservice.exception;

/**
 * Thrown when an uploaded file violates the domain invariants,
 * such as not being a PDF or exceeding the maximum allowed file size (10MB).
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
