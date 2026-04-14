package com.studyhub.noteservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for note upload requests.
 * Contains the metadata that accompanies a PDF file upload.
 * Validated using Jakarta Bean Validation constraints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteUploadRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Module code is required")
    @Size(min = 2, max = 20, message = "Module code must be between 2 and 20 characters")
    private String moduleCode;

    @Size(max = 100, message = "Module name must not exceed 100 characters")
    private String moduleName;
}
