package com.studyhub.coreservice.module.api;

import com.studyhub.coreservice.module.api.dto.FollowModuleRequest;
import com.studyhub.coreservice.module.application.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/modules", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Modules", description = "Module subscription APIs")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List the modules the authenticated user follows")
    public List<String> listFollowedModules(Authentication authentication) {
        return moduleService.listFollowedModules(authentication.getName());
    }

    @PostMapping(path = "/follow", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Follow a module by code")
    public List<String> followModule(
            @Valid @RequestBody FollowModuleRequest request,
            Authentication authentication
    ) {
        return moduleService.followModule(authentication.getName(), request.moduleCode());
    }

    @DeleteMapping("/{moduleCode}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Unfollow a module by code")
    public List<String> unfollowModule(
            @PathVariable String moduleCode,
            Authentication authentication
    ) {
        return moduleService.unfollowModule(authentication.getName(), moduleCode);
    }
}