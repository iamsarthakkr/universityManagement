package com.sarthak.universityManagement.auth.dto;

import com.sarthak.universityManagement.user.dto.MeResponse;

public record LoginResponse(
    String accessToken,
    MeResponse user
) { }
