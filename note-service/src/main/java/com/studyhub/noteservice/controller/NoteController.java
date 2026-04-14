package com.studyhub.noteservice.controller;

import com.studyhub.noteservice.dto.NoteResponse;
import com.studyhub.noteservice.dto.NoteUploadRequest;
import com.studyhub.noteservice.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse uploadNote(
            @AuthenticationPrincipal UUID uploaderId,
            @RequestPart("metadata") @Valid NoteUploadRequest metadata,
            @RequestPart("file") MultipartFile file
    ) {
        return noteService.uploadNote(uploaderId, metadata, file);
    }

    @GetMapping("/{noteId}")
    public NoteResponse getNoteById(@PathVariable UUID noteId) {
        return noteService.getNoteById(noteId);
    }

    @GetMapping("/{noteId}/download")
    public ResponseEntity<Resource> downloadNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID downloaderId
    ) {
        UUID effectiveDownloaderId = downloaderId != null ? downloaderId : UUID.randomUUID();
        Resource resource = noteService.downloadNote(noteId, effectiveDownloaderId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/my")
    public Page<NoteResponse> getMyNotes(
            @AuthenticationPrincipal UUID uploaderId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return noteService.getNotesByUploader(uploaderId, pageable);
    }

    @GetMapping("/module/{moduleCode}")
    public Page<NoteResponse> getNotesByModule(
            @PathVariable String moduleCode,
            @RequestParam(defaultValue = "RECENT") String sortBy,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return noteService.getNotesByModule(moduleCode, sortBy, pageable);
    }

    @GetMapping("/search")
    public Page<NoteResponse> searchNotes(
            @RequestParam("q") String query,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return noteService.searchNotes(query, pageable);
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(
            @PathVariable UUID noteId,
            @AuthenticationPrincipal UUID requesterId
    ) {
        noteService.deleteNote(noteId, requesterId);
    }
}
