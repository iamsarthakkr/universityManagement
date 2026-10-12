package com.sarthak.universityManagement.security;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.WebIntegrationTests;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RouteSecurityWebTests extends WebIntegrationTests {
    @Autowired
    private UserRepo userRepo;

    @Nested
    class AnonymousAccess {

        @ParameterizedTest(name = "{0} {1} -> {2}")
        @CsvSource({
            "POST, /auth/login,               400",
            "POST, /student-registrations,    400",
            "POST, /instructor-registrations, 400",
            "GET,  /departments,              200",
            "GET,  /courses,                  200",
            "GET,  /courses/2147483647,       404",
        })
        void shouldLetAnonymousReachPublicRoutes(String method, String path, int expectedStatus) throws Exception {
            mockMvc.perform(request(HttpMethod.valueOf(method), path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().is(expectedStatus));
        }

        @ParameterizedTest(name = "{0} {1}")
        @CsvSource({
            "GET,  /me",
            "GET,  /me/enrollments",
            "GET,  /me/course-offerings",
            "GET,  /students",
            "GET,  /instructors",
            "GET,  /student-registrations",
            "GET,  /instructor-registrations",
            "POST, /student-registrations/1/approve",
            "POST, /instructor-registrations/1/approve",
            "GET,  /semesters",
            "GET,  /semesters/1",
            "POST, /semesters",
            "POST, /semesters/1/activate",
            "GET,  /course-offerings",
            "GET,  /course-offerings/1",
            "POST, /course-offerings",
            "GET,  /course-offerings/1/enrollments",
            "POST, /course-offerings/1/enrollments",
            "GET,  /enrollments/1",
            "POST, /enrollments/1/approve",
            "POST, /courses",
        })
        void shouldRejectAnonymousOnProtectedRoutes(String method, String path) throws Exception {
            mockMvc.perform(request(HttpMethod.valueOf(method), path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("Authentication required"));
        }
    }

    @Nested
    class Tokens {

        @Test
        void shouldAcceptValidToken() throws Exception {
            mockMvc.perform(get("/me").with(bearer(Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.body.role").value("ADMIN"));
        }

        @Test
        void shouldRejectMalformedToken() throws Exception {
            mockMvc.perform(get("/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        void shouldRejectTokenOfDeletedUser() throws Exception {
            var user = userSeeder.saveDefault(Role.ADMIN);
            var token = bearer(user);
            userRepo.delete(user);
            userRepo.flush();

            mockMvc.perform(get("/me").with(token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }

    @Nested
    class RoleChecks {

        @ParameterizedTest(name = "{0} on GET {1}")
        @CsvSource({
            "STUDENT,    /students",
            "INSTRUCTOR, /students",
            "STUDENT,    /instructors",
            "STUDENT,    /student-registrations",
            "INSTRUCTOR, /instructor-registrations",
        })
        void shouldReturnForbiddenForWrongRole(Role role, String path) throws Exception {
            mockMvc.perform(get(path).with(bearer(role)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @ParameterizedTest(name = "ADMIN on GET {0}")
        @CsvSource({
            "/students",
            "/instructors",
            "/student-registrations",
            "/instructor-registrations",
        })
        void shouldLetAdminReachAdminLists(String path) throws Exception {
            mockMvc.perform(get(path).with(bearer(Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
        }
    }
}
