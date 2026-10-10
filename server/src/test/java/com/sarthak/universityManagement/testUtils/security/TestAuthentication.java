package com.sarthak.universityManagement.testUtils.security;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.student.StudentEntity;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@TestConfiguration
@Profile("test")
public class TestAuthentication {

    public static void asAdmin() {
        var principal = UserPrincipal
            .builder()
            .username("test-admin")
            .userId(-1)
            .role(Role.ADMIN).enabled(true)
            .build();
        authenticate(principal);
    }

    public static void asStudent(StudentEntity student) {
        var principal = new UserPrincipal(student.getUser());
        authenticate(principal);
    }

    public static void asInstructor(InstructorEntity instructor) {
        var principal = new UserPrincipal(instructor.getUser());
        authenticate(principal);
    }

    public static void asRole(Role role) {
        var principal = UserPrincipal
            .builder()
            .username("test-" + role.name().toLowerCase())
            .userId(-1)
            .role(role).enabled(true)
            .build();
        authenticate(principal);
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticate(UserPrincipal principal) {
        var authentication =
            new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
            );

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }
}
