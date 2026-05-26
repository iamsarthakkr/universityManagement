package com.sarthak.universityManagement.auth.dto;

import com.sarthak.universityManagement.user.dto.UserResponse;

public record LoginResponse(
    String accessToken,
    UserResponse user
) {
}
