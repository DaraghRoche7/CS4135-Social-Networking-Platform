package com.studyhub.noteservice.domain.valueobject;

/**
 * Value object representing the lifecycle status of a Note.
 * <p>
 * ACTIVE - The note is publicly visible and available for download.
 * DELETED - The note has been soft-deleted by the owner and is no longer visible.
 * FLAGGED - The note has been flagged for review (e.g., inappropriate content or copyright issues).
 */
public enum NoteStatus {
    ACTIVE,
    DELETED,
    FLAGGED
}
