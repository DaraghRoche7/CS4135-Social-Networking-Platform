package com.studyhub.noteservice.event;

import com.studyhub.noteservice.domain.event.NoteDeletedEvent;
import com.studyhub.noteservice.domain.event.NoteDownloadedEvent;
import com.studyhub.noteservice.domain.event.NoteUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events to RabbitMQ for consumption by other bounded contexts.
 * Uses a topic exchange ("note.events") with routing keys to allow selective consumption.
 * When RabbitMQ is not available (e.g., local dev profile), events are logged only.
 */
@Component
public class NoteEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoteEventPublisher.class);

    private static final String EXCHANGE_NAME = "note.events";
    private static final String ROUTING_KEY_UPLOADED = "note.uploaded";
    private static final String ROUTING_KEY_DOWNLOADED = "note.downloaded";
    private static final String ROUTING_KEY_DELETED = "note.deleted";

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    public void publishNoteUploaded(NoteUploadedEvent event) {
        if (rabbitTemplate == null) {
            log.info("[LOCAL] NoteUploadedEvent: noteId={}, uploader={}, module={}",
                    event.noteId(), event.uploaderId(), event.moduleCode());
            return;
        }
        log.info("Publishing NoteUploadedEvent for note: {} by user: {}", event.noteId(), event.uploaderId());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY_UPLOADED, event);
    }

    public void publishNoteDownloaded(NoteDownloadedEvent event) {
        if (rabbitTemplate == null) {
            log.info("[LOCAL] NoteDownloadedEvent: noteId={}, downloader={}", event.noteId(), event.downloaderId());
            return;
        }
        log.info("Publishing NoteDownloadedEvent for note: {} by user: {}", event.noteId(), event.downloaderId());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY_DOWNLOADED, event);
    }

    public void publishNoteDeleted(NoteDeletedEvent event) {
        if (rabbitTemplate == null) {
            log.info("[LOCAL] NoteDeletedEvent: noteId={}, uploader={}", event.noteId(), event.uploaderId());
            return;
        }
        log.info("Publishing NoteDeletedEvent for note: {} by user: {}", event.noteId(), event.uploaderId());
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY_DELETED, event);
    }
}
