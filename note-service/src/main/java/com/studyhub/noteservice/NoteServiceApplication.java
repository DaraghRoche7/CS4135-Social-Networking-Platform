package com.studyhub.noteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the StudyHub Note Service.
 * This microservice handles PDF note upload, download, metadata storage,
 * and filtering by module and popularity within the StudyHub academic platform.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NoteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteServiceApplication.class, args);
    }
}
