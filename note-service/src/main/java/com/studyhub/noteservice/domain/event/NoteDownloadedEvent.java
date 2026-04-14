package com.studyhub.noteservice.domain.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a note is downloaded by a user.
 * Consumed by the Notification bounded context to notify the note owner
 * and by analytics services for popularity tracking.
 *
 * @param noteId       the unique identifier of the downloaded note
 * @param downloaderId the unique identifier of the user who downloaded the note
 * @param timestamp    the time at which the download occurred
 */
public record NoteDownloadedEvent(
        UUID noteId,
        UUID downloaderId,
        LocalDateTime timestamp
) implements Serializable {
}
