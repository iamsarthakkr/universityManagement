package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.config.WebIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SeedLoginWebTests extends WebIntegrationTests {
    @Autowired
    private SeedApplier seedApplier;

    @Test
    void seededUsersCanLogIn() throws Exception {
        seedApplier.apply(SeedFiles.example(), true);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"riya.sharma\",\"password\":\"password\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.body.user.role").value("STUDENT"));
    }
}
