package com.studyhub.interaction.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
public class NotesServiceClient {

    private final RestTemplate restTemplate;
    private final String notesServiceUrl;

    public NotesServiceClient(
            RestTemplate restTemplate,
            @Value("${services.notes-service.url:http://localhost:8082}") String notesServiceUrl) {
        this.restTemplate = restTemplate;
        this.notesServiceUrl = notesServiceUrl;
    }

    @CircuitBreaker(name = "notesService", fallbackMethod = "postExistsFallback")
    public boolean postExists(UUID postId) {
        try {
            restTemplate.getForObject(notesServiceUrl + "/api/posts/" + postId, Object.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    public boolean postExistsFallback(UUID postId, Exception ex) {
        log.warn("Notes service unavailable when checking postId={}. Proceeding with like anyway.", postId);
        // fallback: assume post exists to avoid blocking likes when notes service is down
        return true;
    }
}
