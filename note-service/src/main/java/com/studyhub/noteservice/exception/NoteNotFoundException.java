package com.studyhub.noteservice.exception;

import java.util.UUID;

/**
 * Thrown when a requested note cannot be found in the system,
 * either because it does not exist or has been soft-deleted.
 */
public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(UUID noteId) {
        super("Note not found with id: " + noteId);
    }

    public NoteNotFoundException(String message) {
        super(message);
    }
}
