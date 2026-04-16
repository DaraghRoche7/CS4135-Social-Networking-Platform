package com.studyhub.coreservice.post.api;

import com.studyhub.coreservice.post.api.dto.CreatePostRequest;
import com.studyhub.coreservice.post.api.dto.PostListResponse;
import com.studyhub.coreservice.post.api.dto.PostResponse;
import com.studyhub.coreservice.post.api.dto.UpdatePostRequest;
import com.studyhub.coreservice.post.application.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Posts", description = "PDF note upload and engagement APIs")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List uploaded posts")
    public PostListResponse getPosts(
        @RequestParam(required = false) String module,
        Authentication authentication
    ) {
        return postService.getPosts(authentication.getName(), module);
    }

    @GetMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get a single post")
    public PostResponse getPost(@PathVariable Long postId, Authentication authentication) {
        return postService.getPost(postId, authentication.getName());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Upload a PDF post")
    public PostResponse createPost(
        @Valid @ModelAttribute CreatePostRequest request,
        Authentication authentication
    ) {
        return postService.createPost(request, authentication.getName());
    }

    @PutMapping(path = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Update a post's metadata")
    public PostResponse updatePost(
        @PathVariable Long postId,
        @Valid @RequestBody UpdatePostRequest request,
        Authentication authentication
    ) {
        return postService.updatePost(postId, request, authentication.getName(), isAdmin(authentication));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Delete a post")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, Authentication authentication) {
        postService.deletePost(postId, authentication.getName(), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/like")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Like a post")
    public PostResponse likePost(@PathVariable Long postId, Authentication authentication) {
        return postService.likePost(postId, authentication.getName());
    }

    @DeleteMapping("/{postId}/like")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Unlike a post")
    public PostResponse unlikePost(@PathVariable Long postId, Authentication authentication) {
        return postService.unlikePost(postId, authentication.getName());
    }

    @GetMapping(path = "/{postId}/file", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Download the original PDF file for a post")
    public ResponseEntity<Resource> downloadPostFile(@PathVariable Long postId) {
        PostService.PostFileDownload download = postService.downloadPostFile(postId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(download.contentType()))
            .contentLength(download.fileSize())
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(download.originalFileName()).build().toString()
            )
            .body(download.resource());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
