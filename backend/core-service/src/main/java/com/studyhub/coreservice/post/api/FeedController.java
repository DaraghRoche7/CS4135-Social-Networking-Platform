package com.studyhub.coreservice.post.api;

import com.studyhub.coreservice.post.api.dto.PostListResponse;
import com.studyhub.coreservice.post.application.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/feed", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Feed", description = "Personalized feed APIs")
public class FeedController {

    private final PostService postService;

    public FeedController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get the authenticated user's feed")
    public PostListResponse getFeed(
        @RequestParam(required = false) String module,
        Authentication authentication
    ) {
        return postService.getFeed(authentication.getName(), module);
    }
}
