package com.sarthak.universityManagement.testUtils.security;

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

    public static void asStudent(StudentEntity student) {
        var principal = new UserPrincipal(student.getUser());
        authenticate(principal);
    }

    public static void asInstructor(InstructorEntity instructor) {
        var principal = new UserPrincipal(instructor.getUser());
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
