package com.sarthak.universityManagement.user.dto;

import com.sarthak.universityManagement.common.types.Role;
import lombok.Builder;

@Builder
public record CreateUserCommand(
    String username,
    String hashedPassword,
    String email,
    Role role
) {
}
