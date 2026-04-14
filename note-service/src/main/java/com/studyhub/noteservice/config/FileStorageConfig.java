package com.studyhub.noteservice.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for local file storage.
 * Ensures the upload directory exists at application startup.
 * The upload directory path is read from the "file.upload-dir" property.
 */
@Configuration
public class FileStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(FileStorageConfig.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Creates the upload directory on application startup if it does not already exist.
     *
     * @throws IOException if the directory cannot be created
     */
    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory at: {}", uploadPath);
        } else {
            log.info("Upload directory already exists at: {}", uploadPath);
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }
}
