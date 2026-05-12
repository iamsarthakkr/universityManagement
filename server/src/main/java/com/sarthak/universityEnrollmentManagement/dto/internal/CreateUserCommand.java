package com.sarthak.universityEnrollmentManagement.dto.internal;

import com.sarthak.universityEnrollmentManagement.entity.types.Role;
import lombok.Builder;

@Builder
public record CreateUserCommand(
    String username,
    String hashedPassword,
    String email,
    Role role
) {
}
