package com.sarthak.universityManagement.user.dto;

import com.sarthak.universityManagement.common.types.Role;
import lombok.Builder;

@Builder
public record MeResponse(
    Integer id,
    String username,
    String email,
    Role role,
    Profile profile     // null for admins
) { }
