package com.studyhub.userservice.controller;

import com.studyhub.userservice.dto.UpdateProfileRequest;
import com.studyhub.userservice.dto.UserProfileResponse;
import com.studyhub.userservice.dto.UserSummaryResponse;
import com.studyhub.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getProfile(id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getUsername(), request));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> follow(
        @AuthenticationPrincipal UserDetails principal,
        @PathVariable UUID id
    ) {
        userService.follow(principal.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unfollow(
        @AuthenticationPrincipal UserDetails principal,
        @PathVariable UUID id
    ) {
        userService.unfollow(principal.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserSummaryResponse>> getFollowers(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getFollowers(id));
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserSummaryResponse>> getFollowing(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getFollowing(id));
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> search(@RequestParam(required = false, defaultValue = "") String name) {
        return ResponseEntity.ok(userService.search(name));
    }
}