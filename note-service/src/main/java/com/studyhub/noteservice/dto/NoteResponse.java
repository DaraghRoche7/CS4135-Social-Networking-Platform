package com.studyhub.noteservice.dto;

import com.studyhub.noteservice.domain.entity.Note;
import com.studyhub.noteservice.domain.valueobject.NoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO that maps from the Note entity to the API response.
 * Excludes internal storage details like file system paths.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteResponse {

    private UUID id;
    private UUID uploaderId;
    private String title;
    private String description;
    private String moduleCode;
    private String moduleName;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Long downloadCount;
    private NoteStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create a NoteResponse from a Note entity.
     * Maps all publicly-visible fields while excluding internal storage paths.
     *
     * @param note the Note entity to map from
     * @return a NoteResponse DTO
     */
    public static NoteResponse fromEntity(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .uploaderId(note.getUploaderId())
                .title(note.getTitle())
                .description(note.getDescription())
                .moduleCode(note.getModuleCode())
                .moduleName(note.getModuleName())
                .fileName(note.getFileName())
                .fileSize(note.getFileSize())
                .mimeType(note.getMimeType())
                .downloadCount(note.getDownloadCount())
                .status(note.getStatus())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
