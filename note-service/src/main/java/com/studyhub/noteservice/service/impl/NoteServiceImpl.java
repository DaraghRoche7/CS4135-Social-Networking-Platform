package com.studyhub.noteservice.service.impl;

import com.studyhub.noteservice.domain.entity.Note;
import com.studyhub.noteservice.domain.event.NoteDeletedEvent;
import com.studyhub.noteservice.domain.event.NoteDownloadedEvent;
import com.studyhub.noteservice.domain.event.NoteUploadedEvent;
import com.studyhub.noteservice.domain.valueobject.NoteStatus;
import com.studyhub.noteservice.dto.NoteResponse;
import com.studyhub.noteservice.dto.NoteUploadRequest;
import com.studyhub.noteservice.event.NoteEventPublisher;
import com.studyhub.noteservice.exception.InvalidFileException;
import com.studyhub.noteservice.exception.NoteNotFoundException;
import com.studyhub.noteservice.exception.UnauthorizedAccessException;
import com.studyhub.noteservice.repository.NoteRepository;
import com.studyhub.noteservice.service.NoteService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the NoteService domain service.
 * Coordinates file storage, database persistence, and event publishing
 * for all note-related operations within the Notes bounded context.
 */
@Service
@Transactional
public class NoteServiceImpl implements NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteServiceImpl.class);
    private static final String APPLICATION_PDF = "application/pdf";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes

    private final NoteRepository noteRepository;
    private final NoteEventPublisher noteEventPublisher;
    private final Path fileStoragePath;

    public NoteServiceImpl(
            NoteRepository noteRepository,
            NoteEventPublisher noteEventPublisher,
            @Value("${file.upload-dir}") String uploadDir
    ) {
        this.noteRepository = noteRepository;
        this.noteEventPublisher = noteEventPublisher;
        this.fileStoragePath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public NoteResponse uploadNote(UUID uploaderId, NoteUploadRequest request, MultipartFile file) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "note.pdf";

        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        Path targetPath = fileStoragePath.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {} for uploader: {}", storedFileName, uploaderId);
        } catch (IOException e) {
            log.error("Failed to store file: {}", storedFileName, e);
            throw new RuntimeException("Failed to store uploaded file", e);
        }

        Note note = Note.builder()
                .uploaderId(uploaderId)
                .title(request.getTitle())
                .description(request.getDescription())
                .moduleCode(request.getModuleCode())
                .moduleName(request.getModuleName())
                .fileName(originalFileName)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .storagePath(targetPath.toString())
                .downloadCount(0L)
                .status(NoteStatus.ACTIVE)
                .build();

        Note savedNote = noteRepository.save(note);
        log.info("Note saved with id: {} for module: {}", savedNote.getId(), savedNote.getModuleCode());

        publishNoteUploadedEvent(savedNote);

        return NoteResponse.fromEntity(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadNote(UUID noteId, UUID downloaderId) {
        Note note = noteRepository.findByIdAndStatus(noteId, NoteStatus.ACTIVE)
                .orElseThrow(() -> new NoteNotFoundException(noteId));

        note.incrementDownloadCount();
        noteRepository.save(note);

        publishNoteDownloadedEvent(noteId, downloaderId);

        try {
            Path filePath = Paths.get(note.getStoragePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("Serving file download for note: {} to user: {}", noteId, downloaderId);
                return resource;
            } else {
                log.error("File not found or not readable at path: {}", note.getStoragePath());
                throw new NoteNotFoundException("File not found for note: " + noteId);
            }
        } catch (MalformedURLException e) {
            log.error("Malformed file path for note: {}", noteId, e);
            throw new NoteNotFoundException("File not found for note: " + noteId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(UUID noteId) {
        Note note = noteRepository.findByIdAndStatus(noteId, NoteStatus.ACTIVE)
                .orElseThrow(() -> new NoteNotFoundException(noteId));
        return NoteResponse.fromEntity(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> getNotesByUploader(UUID uploaderId, Pageable pageable) {
        return noteRepository.findByUploaderIdAndStatus(uploaderId, NoteStatus.ACTIVE, pageable)
                .map(NoteResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> getNotesByModule(String moduleCode, String sortBy, Pageable pageable) {
        if ("POPULAR".equalsIgnoreCase(sortBy)) {
            return noteRepository
                    .findByModuleCodeAndStatusOrderByDownloadCountDesc(moduleCode, NoteStatus.ACTIVE, pageable)
                    .map(NoteResponse::fromEntity);
        }
        return noteRepository
                .findByModuleCodeAndStatus(moduleCode, NoteStatus.ACTIVE, pageable)
                .map(NoteResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> searchNotes(String query, Pageable pageable) {
        return noteRepository
                .searchByTitleContainingIgnoreCaseAndStatus(query, NoteStatus.ACTIVE, pageable)
                .map(NoteResponse::fromEntity);
    }

    @Override
    public void deleteNote(UUID noteId, UUID requesterId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));

        if (!note.isOwnedBy(requesterId)) {
            throw new UnauthorizedAccessException(
                    "User " + requesterId + " is not authorized to delete note " + noteId
            );
        }

        note.markAsDeleted();
        noteRepository.save(note);
        log.info("Note {} soft-deleted by user {}", noteId, requesterId);

        publishNoteDeletedEvent(note);
    }

    /**
     * Validates that the uploaded file is a non-empty PDF under 10MB.
     * Enforces the domain invariant that only PDF files are accepted.
     *
     * @param file the uploaded file to validate
     * @throws InvalidFileException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required and must not be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(APPLICATION_PDF)) {
            throw new InvalidFileException("Only PDF files are accepted. Received: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    "File size exceeds the maximum allowed size of 10MB. Received: "
                            + (file.getSize() / (1024 * 1024)) + "MB"
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileException("File must have a .pdf extension");
        }
    }

    @CircuitBreaker(name = "noteEventPublisher", fallbackMethod = "fallbackPublishNoteUploaded")
    private void publishNoteUploadedEvent(Note note) {
        NoteUploadedEvent event = new NoteUploadedEvent(
                note.getId(),
                note.getUploaderId(),
                note.getTitle(),
                note.getModuleCode(),
                LocalDateTime.now()
        );
        noteEventPublisher.publishNoteUploaded(event);
    }

    @CircuitBreaker(name = "noteEventPublisher", fallbackMethod = "fallbackPublishNoteDownloaded")
    private void publishNoteDownloadedEvent(UUID noteId, UUID downloaderId) {
        NoteDownloadedEvent event = new NoteDownloadedEvent(
                noteId,
                downloaderId,
                LocalDateTime.now()
        );
        noteEventPublisher.publishNoteDownloaded(event);
    }

    @CircuitBreaker(name = "noteEventPublisher", fallbackMethod = "fallbackPublishNoteDeleted")
    private void publishNoteDeletedEvent(Note note) {
        NoteDeletedEvent event = new NoteDeletedEvent(
                note.getId(),
                note.getUploaderId(),
                note.getTitle(),
                note.getModuleCode(),
                LocalDateTime.now()
        );
        noteEventPublisher.publishNoteDeleted(event);
    }

    /**
     * Fallback method invoked when the circuit breaker is open for upload event publishing.
     * Logs the failure but does not fail the upload operation, ensuring eventual consistency.
     */
    private void fallbackPublishNoteUploaded(Note note, Throwable throwable) {
        log.error("Circuit breaker open: Failed to publish NoteUploadedEvent for note: {}. Error: {}",
                note.getId(), throwable.getMessage());
    }

    /**
     * Fallback method invoked when the circuit breaker is open for download event publishing.
     */
    private void fallbackPublishNoteDownloaded(UUID noteId, UUID downloaderId, Throwable throwable) {
        log.error("Circuit breaker open: Failed to publish NoteDownloadedEvent for note: {}. Error: {}",
                noteId, throwable.getMessage());
    }

    /**
     * Fallback method invoked when the circuit breaker is open for delete event publishing.
     */
    private void fallbackPublishNoteDeleted(Note note, Throwable throwable) {
        log.error("Circuit breaker open: Failed to publish NoteDeletedEvent for note: {}. Error: {}",
                note.getId(), throwable.getMessage());
    }
}
