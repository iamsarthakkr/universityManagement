package com.sarthak.universityManagement.user.dto;

import com.sarthak.universityManagement.common.types.Role;

public record UserResponse(Integer id, String username, Role role) {
}
