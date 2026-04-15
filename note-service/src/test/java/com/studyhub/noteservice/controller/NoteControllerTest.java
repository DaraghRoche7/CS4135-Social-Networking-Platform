package com.studyhub.noteservice.controller;

import com.studyhub.noteservice.domain.valueobject.NoteStatus;
import com.studyhub.noteservice.dto.NoteResponse;
import com.studyhub.noteservice.security.JwtAuthenticationFilter;
import com.studyhub.noteservice.security.JwtTokenProvider;
import com.studyhub.noteservice.service.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private NoteResponse createSampleResponse() {
        return NoteResponse.builder()
                .id(UUID.randomUUID())
                .uploaderId(UUID.randomUUID())
                .title("Week 7 - DDD Patterns")
                .description("Covers entities and aggregates")
                .moduleCode("CS4135")
                .moduleName("Software Architectures")
                .fileName("notes.pdf")
                .fileSize(2048L)
                .mimeType("application/pdf")
                .downloadCount(10L)
                .status(NoteStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getNoteById_shouldReturnNote() throws Exception {
        NoteResponse response = createSampleResponse();
        when(noteService.getNoteById(response.getId())).thenReturn(response);

        mockMvc.perform(get("/api/notes/{noteId}", response.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Week 7 - DDD Patterns"))
                .andExpect(jsonPath("$.moduleCode").value("CS4135"));
    }

    @Test
    void getNotesByModule_shouldReturnFilteredResults() throws Exception {
        NoteResponse response = createSampleResponse();
        when(noteService.getNotesByModule(eq("CS4135"), eq("RECENT"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/notes/module/CS4135")
                        .param("sortBy", "RECENT")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].moduleCode").value("CS4135"));
    }

    @Test
    void searchNotes_shouldReturnResults() throws Exception {
        NoteResponse response = createSampleResponse();
        when(noteService.searchNotes(eq("DDD"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/notes/search")
                        .param("q", "DDD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Week 7 - DDD Patterns"));
    }

    @Test
    @WithMockUser
    void deleteNote_shouldReturnNoContent() throws Exception {
        UUID noteId = UUID.randomUUID();
        doNothing().when(noteService).deleteNote(eq(noteId), any());

        mockMvc.perform(delete("/api/notes/{noteId}", noteId))
                .andExpect(status().isNoContent());
    }
}
