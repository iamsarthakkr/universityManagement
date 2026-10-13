package com.sarthak.universityManagement.security;

import com.sarthak.universityManagement.config.WebIntegrationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000,http://localhost:5173")
class CorsWebTests extends WebIntegrationTests {

    @Test
    void shouldAllowEveryConfiguredOrigin() throws Exception {
        for (var origin : new String[] {"http://localhost:3000", "http://localhost:5173"}) {
            mockMvc.perform(preflight(origin))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin));
        }
    }

    @Test
    void shouldRejectAnOriginThatIsNotConfigured() throws Exception {
        mockMvc.perform(preflight("https://evil.example.com"))
            .andExpect(status().isForbidden());
    }

    private static MockHttpServletRequestBuilder preflight(String origin) {
        return options("/auth/login")
            .header(HttpHeaders.ORIGIN, origin)
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
    }
}
