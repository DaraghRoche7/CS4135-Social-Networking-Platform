package com.studyhub.noteservice.service;

import com.studyhub.noteservice.domain.entity.Note;
import com.studyhub.noteservice.domain.valueobject.NoteStatus;
import com.studyhub.noteservice.dto.NoteResponse;
import com.studyhub.noteservice.dto.NoteUploadRequest;
import com.studyhub.noteservice.event.NoteEventPublisher;
import com.studyhub.noteservice.exception.InvalidFileException;
import com.studyhub.noteservice.exception.NoteNotFoundException;
import com.studyhub.noteservice.exception.UnauthorizedAccessException;
import com.studyhub.noteservice.repository.NoteRepository;
import com.studyhub.noteservice.service.impl.NoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteEventPublisher noteEventPublisher;

    @TempDir
    Path tempDir;

    private NoteServiceImpl noteService;

    private UUID uploaderId;
    private UUID noteId;

    @BeforeEach
    void setUp() {
        noteService = new NoteServiceImpl(noteRepository, noteEventPublisher, tempDir.toString());
        uploaderId = UUID.randomUUID();
        noteId = UUID.randomUUID();
    }

    @Test
    void uploadNote_withValidPdf_shouldSucceed() {
        NoteUploadRequest request = NoteUploadRequest.builder()
                .title("Week 7 - DDD Patterns")
                .description("Covers entities and aggregates")
                .moduleCode("CS4135")
                .moduleName("Software Architectures")
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.pdf", "application/pdf", "PDF content".getBytes()
        );

        Note savedNote = Note.builder()
                .id(noteId)
                .uploaderId(uploaderId)
                .title(request.getTitle())
                .description(request.getDescription())
                .moduleCode(request.getModuleCode())
                .moduleName(request.getModuleName())
                .fileName("notes.pdf")
                .fileSize(file.getSize())
                .mimeType("application/pdf")
                .storagePath(tempDir.resolve("notes.pdf").toString())
                .downloadCount(0L)
                .status(NoteStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        NoteResponse response = noteService.uploadNote(uploaderId, request, file);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Week 7 - DDD Patterns");
        assertThat(response.getModuleCode()).isEqualTo("CS4135");
        assertThat(response.getUploaderId()).isEqualTo(uploaderId);
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void uploadNote_withInvalidFileType_shouldReject() {
        NoteUploadRequest request = NoteUploadRequest.builder()
                .title("Test Note")
                .moduleCode("CS4135")
                .build();

        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", "PNG content".getBytes()
        );

        assertThatThrownBy(() -> noteService.uploadNote(uploaderId, request, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Only PDF files are accepted");

        verify(noteRepository, never()).save(any());
    }

    @Test
    void uploadNote_withOversizedFile_shouldReject() {
        NoteUploadRequest request = NoteUploadRequest.builder()
                .title("Test Note")
                .moduleCode("CS4135")
                .build();

        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", largeContent
        );

        assertThatThrownBy(() -> noteService.uploadNote(uploaderId, request, file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("exceeds the maximum allowed size");

        verify(noteRepository, never()).save(any());
    }

    @Test
    void deleteNote_byOwner_shouldSucceed() {
        Note note = Note.builder()
                .id(noteId)
                .uploaderId(uploaderId)
                .title("My Note")
                .moduleCode("CS4135")
                .status(NoteStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        noteService.deleteNote(noteId, uploaderId);

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NoteStatus.DELETED);
    }

    @Test
    void deleteNote_byNonOwner_shouldThrowUnauthorized() {
        UUID otherUserId = UUID.randomUUID();
        Note note = Note.builder()
                .id(noteId)
                .uploaderId(uploaderId)
                .title("My Note")
                .moduleCode("CS4135")
                .status(NoteStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(noteRepository.findById(noteId)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.deleteNote(noteId, otherUserId))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(noteRepository, never()).save(any());
    }

    @Test
    void getNotesByModule_shouldReturnFilteredResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Note note1 = Note.builder()
                .id(UUID.randomUUID())
                .uploaderId(uploaderId)
                .title("Note 1")
                .moduleCode("CS4135")
                .fileName("note1.pdf")
                .fileSize(1000L)
                .mimeType("application/pdf")
                .storagePath("/path")
                .downloadCount(5L)
                .status(NoteStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page<Note> page = new PageImpl<>(List.of(note1));
        when(noteRepository.findByModuleCodeAndStatus("CS4135", NoteStatus.ACTIVE, pageable))
                .thenReturn(page);

        Page<NoteResponse> result = noteService.getNotesByModule("CS4135", "RECENT", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getModuleCode()).isEqualTo("CS4135");
    }

    @Test
    void getNoteById_withNonExistentId_shouldThrowNotFound() {
        UUID missingId = UUID.randomUUID();
        when(noteRepository.findByIdAndStatus(missingId, NoteStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getNoteById(missingId))
                .isInstanceOf(NoteNotFoundException.class);
    }
}
