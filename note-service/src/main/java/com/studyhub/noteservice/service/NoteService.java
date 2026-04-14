package com.studyhub.noteservice.service;

import com.studyhub.noteservice.dto.NoteResponse;
import com.studyhub.noteservice.dto.NoteUploadRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Domain service interface for the Notes bounded context.
 * Defines the operations available for note management including upload,
 * download, retrieval, filtering, search, and deletion.
 */
public interface NoteService {

    /**
     * Uploads a new note with PDF file and metadata.
     * Validates that the file is a PDF and under 10MB, stores the file to local
     * storage, persists metadata to the database, and publishes a NoteUploadedEvent.
     *
     * @param uploaderId the ID of the authenticated user uploading the note
     * @param request    the note metadata (title, description, moduleCode, moduleName)
     * @param file       the PDF file to upload
     * @return the created note as a response DTO
     */
    NoteResponse uploadNote(UUID uploaderId, NoteUploadRequest request, MultipartFile file);

    /**
     * Downloads a note's PDF file. Increments the download count and
     * publishes a NoteDownloadedEvent.
     *
     * @param noteId       the ID of the note to download
     * @param downloaderId the ID of the user downloading the note
     * @return the file as a Spring Resource for streaming
     */
    Resource downloadNote(UUID noteId, UUID downloaderId);

    /**
     * Retrieves the metadata for a single note by its ID.
     *
     * @param noteId the ID of the note to retrieve
     * @return the note metadata as a response DTO
     */
    NoteResponse getNoteById(UUID noteId);

    /**
     * Retrieves all active notes uploaded by a specific user.
     *
     * @param uploaderId the uploader's user ID
     * @param pageable   pagination parameters
     * @return a page of note response DTOs
     */
    Page<NoteResponse> getNotesByUploader(UUID uploaderId, Pageable pageable);

    /**
     * Retrieves notes for a specific academic module with optional sorting.
     *
     * @param moduleCode the academic module code (e.g., "CS4337")
     * @param sortBy     the sort order: "POPULAR" for download count, "RECENT" for creation date
     * @param pageable   pagination parameters
     * @return a page of note response DTOs
     */
    Page<NoteResponse> getNotesByModule(String moduleCode, String sortBy, Pageable pageable);

    /**
     * Searches for notes whose titles contain the given query string.
     *
     * @param query    the search query to match against note titles
     * @param pageable pagination parameters
     * @return a page of note response DTOs matching the search query
     */
    Page<NoteResponse> searchNotes(String query, Pageable pageable);

    /**
     * Soft-deletes a note. Only the note's owner can delete it.
     * Publishes a NoteDeletedEvent upon successful deletion.
     *
     * @param noteId      the ID of the note to delete
     * @param requesterId the ID of the user requesting deletion
     */
    void deleteNote(UUID noteId, UUID requesterId);
}
