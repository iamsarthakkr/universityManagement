package com.sarthak.universityManagement.common;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.config.WebIntegrationTests;
import com.sarthak.universityManagement.registration.student.StudentRegistrationEntity;
import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import com.sarthak.universityManagement.testUtils.fixtures.StudentRegistrationFixtures;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentRegistrationSeeder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RequestBindingWebTests extends WebIntegrationTests {
    @Autowired
    private StudentRegistrationSeeder registrationSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;

    private int registrationCounter = 0;

    private StudentRegistrationEntity saveRegistration(RegistrationStatus status) {
        var n = ++registrationCounter;
        return registrationSeeder.saveStudentRegistration(
            StudentRegistrationFixtures.studentRegistration()
                .department(departmentSeeder.saveDefault("dep-" + n))
                .registrationStatus(status)
                .username("registrant-" + n)
                .email("registrant-" + n + "@test.com")
                .build()
        );
    }

    private RequestPostProcessor admin() {
        return bearer(Role.ADMIN);
    }

    @Nested
    class ActionPath {

        @ParameterizedTest(name = "/{0}")
        @ValueSource(strings = {"approve", "APPROVE", "Approve"})
        void shouldBindActionCaseInsensitively(String action) throws Exception {
            var registration = saveRegistration(RegistrationStatus.PENDING);

            mockMvc.perform(post("/student-registrations/{id}/{action}", registration.getId(), action).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.status").value("APPROVED"));
        }

        @Test
        void shouldRejectUnknownActionWithAllowedValues() throws Exception {
            var registration = saveRegistration(RegistrationStatus.PENDING);

            mockMvc.perform(post("/student-registrations/{id}/foo", registration.getId()).with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'foo' for 'action'. Allowed values: APPROVE, REJECT"));
        }
    }

    @Nested
    class FilterParams {

        @Test
        void shouldBindStatusCaseInsensitively() throws Exception {
            var pending = saveRegistration(RegistrationStatus.PENDING);
            saveRegistration(RegistrationStatus.APPROVED);

            mockMvc.perform(get("/student-registrations").param("status", "pending").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body[*].id", contains(pending.getId())));
        }

        @Test
        void shouldIgnoreUnknownParams() throws Exception {
            saveRegistration(RegistrationStatus.PENDING);

            mockMvc.perform(get("/student-registrations").param("foo", "bar").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body.length()").value(1));
        }

        @Test
        void shouldRejectUnknownStatusWithReadableMessage() throws Exception {
            mockMvc.perform(get("/student-registrations").param("status", "foo").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").value("Invalid value 'foo' for 'status'. Allowed values: PENDING, APPROVED, REJECTED"));
        }

        @Test
        void shouldRejectNonNumericIdWithReadableMessage() throws Exception {
            mockMvc.perform(get("/student-registrations").param("departmentId", "abc").with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.departmentId").value("Invalid value 'abc' for 'departmentId'"));
        }
    }
}
