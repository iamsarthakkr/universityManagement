package com.sarthak.universityManagement.config;

import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class PublicEndpointConfig {
    
    @Bean
    RequestMatcher publicEndpoints() {
        return new OrRequestMatcher(
            PathPatternRequestMatcher.pathPattern(HttpMethod.OPTIONS, "/**"),
            PathPatternRequestMatcher.pathPattern("/auth/login"),
            PathPatternRequestMatcher.pathPattern("/registration/student"),
            PathPatternRequestMatcher.pathPattern("/registration/instructor"),
            PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/departments")
        );
    }
}
