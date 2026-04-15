package com.studyhub.coreservice.auth.api;

import com.studyhub.coreservice.auth.api.dto.UserSummaryResponse;
import com.studyhub.coreservice.auth.application.UserDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Internal Users", description = "Service-to-service user directory endpoints")
public class UserDirectoryController {

    private final UserDirectoryService userDirectoryService;

    public UserDirectoryController(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('SERVICE')")
    @Operation(summary = "Get a user summary by public user id")
    public UserSummaryResponse getUser(@PathVariable String userId) {
        return userDirectoryService.getUserById(userId);
    }
}
