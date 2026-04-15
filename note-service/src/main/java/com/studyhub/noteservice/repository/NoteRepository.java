package com.studyhub.noteservice.repository;

import com.studyhub.noteservice.domain.entity.Note;
import com.studyhub.noteservice.domain.valueobject.NoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Note aggregate root.
 * Provides custom query methods for filtering notes by uploader, module, status,
 * and for search functionality.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    /**
     * Finds all notes uploaded by a specific user with a given status.
     *
     * @param uploaderId the uploader's user ID
     * @param status     the note status to filter by
     * @param pageable   pagination parameters
     * @return a page of notes matching the criteria
     */
    Page<Note> findByUploaderIdAndStatus(UUID uploaderId, NoteStatus status, Pageable pageable);

    /**
     * Finds all notes for a specific academic module with a given status.
     *
     * @param moduleCode the academic module code (e.g., "CS4337")
     * @param status     the note status to filter by
     * @param pageable   pagination parameters
     * @return a page of notes matching the criteria
     */
    Page<Note> findByModuleCodeAndStatus(String moduleCode, NoteStatus status, Pageable pageable);

    /**
     * Finds all notes with a given status.
     *
     * @param status   the note status to filter by
     * @param pageable pagination parameters
     * @return a page of notes matching the criteria
     */
    Page<Note> findByStatus(NoteStatus status, Pageable pageable);

    /**
     * Finds notes for a specific module sorted by download count in descending order.
     * Used for the "popular" sort option when browsing module notes.
     *
     * @param moduleCode the academic module code
     * @param status     the note status to filter by
     * @param pageable   pagination parameters
     * @return a page of notes ordered by popularity
     */
    Page<Note> findByModuleCodeAndStatusOrderByDownloadCountDesc(String moduleCode, NoteStatus status, Pageable pageable);

    /**
     * Searches for notes whose title contains the given query string (case-insensitive).
     *
     * @param title    the search query to match against note titles
     * @param status   the note status to filter by
     * @param pageable pagination parameters
     * @return a page of notes whose titles match the search query
     */
    Page<Note> searchByTitleContainingIgnoreCaseAndStatus(String title, NoteStatus status, Pageable pageable);

    /**
     * Finds a single note by its ID and status.
     * Used to ensure deleted/flagged notes are not returned in normal queries.
     *
     * @param id     the note ID
     * @param status the note status to filter by
     * @return an optional containing the note if found
     */
    Optional<Note> findByIdAndStatus(UUID id, NoteStatus status);
}
