package com.studyhub.noteservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the Note Service.
 * Declares the topic exchange, queues, and bindings used for publishing domain events.
 * <p>
 * Exchange: "note.events" (TopicExchange)
 * Queues:
 * - note.uploaded.queue bound with routing key "note.uploaded"
 * - note.downloaded.queue bound with routing key "note.downloaded"
 * - note.deleted.queue bound with routing key "note.deleted"
 * <p>
 * Messages are serialized as JSON using Jackson2JsonMessageConverter.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "note.events";
    public static final String QUEUE_NOTE_UPLOADED = "note.uploaded.queue";
    public static final String QUEUE_NOTE_DOWNLOADED = "note.downloaded.queue";
    public static final String QUEUE_NOTE_DELETED = "note.deleted.queue";
    public static final String ROUTING_KEY_UPLOADED = "note.uploaded";
    public static final String ROUTING_KEY_DOWNLOADED = "note.downloaded";
    public static final String ROUTING_KEY_DELETED = "note.deleted";

    @Bean
    public TopicExchange noteEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue noteUploadedQueue() {
        return new Queue(QUEUE_NOTE_UPLOADED, true);
    }

    @Bean
    public Queue noteDownloadedQueue() {
        return new Queue(QUEUE_NOTE_DOWNLOADED, true);
    }

    @Bean
    public Queue noteDeletedQueue() {
        return new Queue(QUEUE_NOTE_DELETED, true);
    }

    @Bean
    public Binding bindingNoteUploaded(Queue noteUploadedQueue, TopicExchange noteEventsExchange) {
        return BindingBuilder
                .bind(noteUploadedQueue)
                .to(noteEventsExchange)
                .with(ROUTING_KEY_UPLOADED);
    }

    @Bean
    public Binding bindingNoteDownloaded(Queue noteDownloadedQueue, TopicExchange noteEventsExchange) {
        return BindingBuilder
                .bind(noteDownloadedQueue)
                .to(noteEventsExchange)
                .with(ROUTING_KEY_DOWNLOADED);
    }

    @Bean
    public Binding bindingNoteDeleted(Queue noteDeletedQueue, TopicExchange noteEventsExchange) {
        return BindingBuilder
                .bind(noteDeletedQueue)
                .to(noteEventsExchange)
                .with(ROUTING_KEY_DELETED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
