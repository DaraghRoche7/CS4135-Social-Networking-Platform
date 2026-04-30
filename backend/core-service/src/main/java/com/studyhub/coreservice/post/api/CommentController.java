package com.studyhub.coreservice.post.api;

import com.studyhub.coreservice.post.api.dto.CommentResponse;
import com.studyhub.coreservice.post.api.dto.CreateCommentRequest;
import com.studyhub.coreservice.post.application.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Comments", description = "Post comment APIs")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List comments on a post")
    public List<CommentResponse> listComments(@PathVariable Long postId) {
        return commentService.listComments(postId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Add a comment to a post")
    public CommentResponse createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication
    ) {
        return commentService.createComment(postId, request, authentication.getName());
    }
}