package com.studyhub.noteservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for filtering and sorting notes.
 * Used when browsing notes by module or searching across all notes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteFilterRequest {

    private String moduleCode;

    /**
     * Sort order for results. RECENT sorts by creation date descending,
     * POPULAR sorts by download count descending.
     */
    private String sortBy;

    /**
     * Free-text search query matched against note titles.
     */
    private String query;
}
