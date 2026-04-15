package com.studyhub.coreservice.user.api;

import com.studyhub.coreservice.user.api.dto.UpdateUserProfileRequest;
import com.studyhub.coreservice.user.api.dto.UserListResponse;
import com.studyhub.coreservice.user.api.dto.UserProfileResponse;
import com.studyhub.coreservice.user.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User profile and social graph APIs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Get a user profile")
    public UserProfileResponse getProfile(@PathVariable String userId) {
        return userService.getProfile(userId);
    }

    @PutMapping(path = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Update a user profile")
    public UserProfileResponse updateProfile(
        @PathVariable String userId,
        @Valid @RequestBody UpdateUserProfileRequest request,
        Authentication authentication
    ) {
        return userService.updateProfile(userId, request, authentication.getName(), isAdmin(authentication));
    }

    @GetMapping("/{userId}/followers")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List followers for a user")
    public UserListResponse getFollowers(@PathVariable String userId) {
        return userService.getFollowers(userId);
    }

    @GetMapping("/{userId}/following")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List accounts a user follows")
    public UserListResponse getFollowing(@PathVariable String userId) {
        return userService.getFollowing(userId);
    }

    @PostMapping("/{userId}/follow")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Follow a user")
    public UserProfileResponse followUser(@PathVariable String userId, Authentication authentication) {
        return userService.followUser(userId, authentication.getName());
    }

    @DeleteMapping("/{userId}/follow")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Unfollow a user")
    public UserProfileResponse unfollowUser(@PathVariable String userId, Authentication authentication) {
        return userService.unfollowUser(userId, authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
