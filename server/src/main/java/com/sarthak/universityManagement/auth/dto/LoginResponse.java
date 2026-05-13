package com.sarthak.universityManagement.auth.dto;

public record LoginResponse(
    String accessToken,
    String tokenHeader
) {
}
