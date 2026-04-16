package com.studyhub.coreservice.post.application;

import com.studyhub.coreservice.config.AppProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostStorageService {

    private final Path storageRoot;

    public PostStorageService(AppProperties appProperties) {
        this.storageRoot = Path.of(appProperties.getStorage().getPostsDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not create posts storage directory", ex);
        }
    }

    public String storePdf(MultipartFile file) {
        requirePdf(file.getOriginalFilename(), file.getContentType());
        String storedFileName = UUID.randomUUID() + ".pdf";
        Path target = resolvePath(storedFileName);
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not store post file", ex);
        }
        return storedFileName;
    }

    public String storeSeedPdf(String fileNamePrefix, byte[] content) {
        String storedFileName = "%s-%s.pdf".formatted(fileNamePrefix, UUID.randomUUID());
        Path target = resolvePath(storedFileName);
        try {
            Files.write(target, content);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not store seed PDF", ex);
        }
        return storedFileName;
    }

    public Resource loadAsResource(String storagePath) {
        Path path = resolvePath(storagePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Requested file is no longer available");
        }
        return new PathResource(path);
    }

    public void deleteIfExists(String storagePath) {
        try {
            Files.deleteIfExists(resolvePath(storagePath));
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not delete post file", ex);
        }
    }

    private void requirePdf(String originalFileName, String contentType) {
        String fileName = originalFileName == null ? "" : originalFileName.toLowerCase(Locale.ROOT);
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!type.equals("application/pdf") && !fileName.endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF uploads are supported");
        }
    }

    private Path resolvePath(String storagePath) {
        Path resolved = storageRoot.resolve(storagePath).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        return resolved;
    }
}
