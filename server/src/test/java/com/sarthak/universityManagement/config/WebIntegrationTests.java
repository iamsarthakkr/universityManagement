package com.sarthak.universityManagement.config;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.security.jwt.JwtService;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import com.sarthak.universityManagement.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@AutoConfigureMockMvc
public abstract class WebIntegrationTests extends IntegrationTests {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected UserSeeder userSeeder;
    @Autowired
    private JwtService jwtService;

    protected RequestPostProcessor bearer(UserEntity user) {
        var token = jwtService.generateToken(new UserPrincipal(user));
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        };
    }

    protected RequestPostProcessor bearer(Role role) {
        return bearer(userSeeder.saveDefault(role));
    }
}
