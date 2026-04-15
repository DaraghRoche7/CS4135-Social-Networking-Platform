package com.studyhub.coreservice.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
    @NotBlank @Email @Size(max = 160) String email,
    @NotBlank @Size(max = 120) String displayName
) {
}
