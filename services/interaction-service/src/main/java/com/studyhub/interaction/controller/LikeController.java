package com.studyhub.interaction.controller;

import com.studyhub.interaction.dto.LikeCountResponse;
import com.studyhub.interaction.dto.LikeResponse;
import com.studyhub.interaction.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> likePost(@PathVariable UUID postId,
                                                  Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        LikeResponse response = likeService.likePost(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable UUID postId,
                                            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        likeService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<LikeCountResponse> getLikes(@PathVariable UUID postId,
                                                       Authentication authentication) {
        UUID userId = authentication != null ? (UUID) authentication.getPrincipal() : null;
        LikeCountResponse response = likeService.getLikeInfo(postId, userId);
        return ResponseEntity.ok(response);
    }
}
