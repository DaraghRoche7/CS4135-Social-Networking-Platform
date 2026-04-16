package com.studyhub.coreservice.user.api.dto;

import com.studyhub.coreservice.auth.api.dto.UserSummaryResponse;
import java.util.List;

public record UserListResponse(List<UserSummaryResponse> items) {
}
