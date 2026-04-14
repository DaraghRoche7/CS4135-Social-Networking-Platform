package com.studyhub.noteservice.domain.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a new note is successfully uploaded.
 * Consumed by the Feed and Notification bounded contexts to inform followers
 * and update activity feeds.
 *
 * @param noteId     the unique identifier of the uploaded note
 * @param uploaderId the unique identifier of the user who uploaded the note
 * @param title      the title of the uploaded note
 * @param moduleCode the academic module code associated with the note
 * @param timestamp  the time at which the upload occurred
 */
public record NoteUploadedEvent(
        UUID noteId,
        UUID uploaderId,
        String title,
        String moduleCode,
        LocalDateTime timestamp
) implements Serializable {
}
