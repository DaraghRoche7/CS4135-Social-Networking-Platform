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
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> follow(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID userId) {
        userService.follow(userDetails.getUsername(), userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<Void> unfollow(@AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID userId) {
        userService.unfollow(userDetails.getUsername(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserSummaryResponse>> getFollowers(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserSummaryResponse>> getFollowing(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getFollowing(userId));
    }
}