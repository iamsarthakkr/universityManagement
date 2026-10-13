package com.sarthak.universityManagement.common;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.WebIntegrationTests;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResponseShapeWebTests extends WebIntegrationTests {
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private CourseSeeder courseSeeder;

    private DepartmentEntity department;

    @BeforeEach
    void setUp() {
        department = departmentSeeder.saveDefault("dep");
    }

    private String courseJson(String code) {
        return """
            {"code": "%s", "title": "Algorithms", "description": "Intro", "credits": 4, "departmentId": %d}
            """.formatted(code, department.getId());
    }

    @Nested
    class Success {

        @Test
        void shouldWrapListInSuccessEnvelope() throws Exception {
            courseSeeder.saveDefault(department);

            mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body[0].department.id").value(department.getId()))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$", not(hasKey("errors"))));
        }

        @Test
        void shouldReturnCreatedForNewResource() throws Exception {
            mockMvc.perform(post("/courses").with(bearer(Role.ADMIN)).contentType(MediaType.APPLICATION_JSON).content(courseJson("CS101")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.message").value("Created successfully"))
                .andExpect(jsonPath("$.body.id").isNumber())
                .andExpect(jsonPath("$.body.code").value("CS101"));
        }

        @Test
        void shouldReturnPublicRegistrationWithoutPasswordAndWithClockTimestamp() throws Exception {
            var body = """
                {"username": "new-student", "password": "secret-pass", "firstName": "Asha", "lastName": "Rao",
                 "email": "asha@test.com", "dateOfBirth": "2004-05-06", "departmentId": %d}
                """.formatted(department.getId());

            mockMvc.perform(post("/student-registrations").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body.status").value("PENDING"))
                .andExpect(jsonPath("$.body.allowedActions", empty()))
                .andExpect(jsonPath("$.body.createdAt").value("2026-01-10T00:00:00Z"))
                .andExpect(jsonPath("$.body.dateOfBirth").value("2004-05-06"))
                .andExpect(jsonPath("$.body", not(hasKey("password"))));
        }
    }

    @Nested
    class Errors {

        @Test
        void shouldListInvalidFieldsInValidationEnvelope() throws Exception {
            mockMvc.perform(post("/courses").with(bearer(Role.ADMIN)).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("Some fields are invalid"))
                .andExpect(jsonPath("$.errors.code").value("code required"))
                .andExpect(jsonPath("$.errors.departmentId").value("department required"))
                .andExpect(jsonPath("$", not(hasKey("body"))));
        }

        @Test
        void shouldReturnNotFoundWithMessageOnly() throws Exception {
            mockMvc.perform(get("/courses/{id}", Integer.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("Course with id: " + Integer.MAX_VALUE + " not found!"))
                .andExpect(jsonPath("$", not(hasKey("errors"))));
        }

        @Test
        void shouldReturnConflictWithDomainMessage() throws Exception {
            var admin = bearer(Role.ADMIN);
            mockMvc.perform(post("/courses").with(admin).contentType(MediaType.APPLICATION_JSON).content(courseJson("CS101")))
                .andExpect(status().isCreated());

            mockMvc.perform(post("/courses").with(admin).contentType(MediaType.APPLICATION_JSON).content(courseJson("CS101")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.message").value("Course with code CS101 already exists!"));
        }

        @Test
        void shouldReturnUnreadableBodyAsBadRequest() throws Exception {
            mockMvc.perform(post("/courses").with(bearer(Role.ADMIN)).contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
        }
    }
}
