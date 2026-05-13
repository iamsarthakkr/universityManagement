package com.sarthak.universityManagement.auth.dto;

public record LoginRequest(
    String username,
    String password
) {
}
