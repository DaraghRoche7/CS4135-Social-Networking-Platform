package com.studyhub.noteservice.domain.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a note is soft-deleted by its owner.
 * Consumed by the Feed bounded context to remove the note from activity feeds
 * and by the Notification context if needed.
 *
 * @param noteId     the unique identifier of the deleted note
 * @param uploaderId the unique identifier of the user who owned the note
 * @param title      the title of the deleted note
 * @param moduleCode the academic module code associated with the note
 * @param timestamp  the time at which the deletion occurred
 */
public record NoteDeletedEvent(
        UUID noteId,
        UUID uploaderId,
        String title,
        String moduleCode,
        LocalDateTime timestamp
) implements Serializable {
}
