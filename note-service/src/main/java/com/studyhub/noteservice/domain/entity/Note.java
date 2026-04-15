package com.studyhub.noteservice.domain.entity;

import com.studyhub.noteservice.domain.valueobject.NoteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Aggregate Root for the Notes bounded context.
 * <p>
 * Represents a PDF study note uploaded by a student. Encapsulates file metadata,
 * module association, and download tracking. Enforces domain invariants such as
 * ownership-based deletion and status transitions.
 */
@Entity
@Table(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "uploader_id", nullable = false)
    private UUID uploaderId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "module_code", nullable = false)
    private String moduleCode;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "download_count", nullable = false)
    @Builder.Default
    private Long downloadCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private NoteStatus status = NoteStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets creation and update timestamps before initial persist.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the modification timestamp before each update.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Increments the download count by one. Called each time a user downloads this note.
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Soft-deletes this note by transitioning its status to DELETED.
     * The note record is preserved in the database but hidden from queries.
     */
    public void markAsDeleted() {
        this.status = NoteStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Flags this note for administrative review.
     * Flagged notes remain in the database but may be hidden from public listings.
     */
    public void flag() {
        this.status = NoteStatus.FLAGGED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks whether the given user is the owner of this note.
     *
     * @param userId the user ID to check
     * @return true if the user is the uploader of this note
     */
    public boolean isOwnedBy(UUID userId) {
        return this.uploaderId.equals(userId);
    }
}
