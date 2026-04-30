package com.studyhub.coreservice.module.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FollowModuleRequest(
        @NotBlank @Size(max = 40) String moduleCode
) {
}