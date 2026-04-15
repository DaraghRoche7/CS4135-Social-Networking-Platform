package com.studyhub.coreservice.post.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
    @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 1000) String description,
    @NotBlank @Size(max = 40) String module
) {
}
