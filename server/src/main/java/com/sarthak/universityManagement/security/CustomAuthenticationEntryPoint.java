package com.sarthak.universityManagement.security;

import com.sarthak.universityManagement.common.rest.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger LOG = LogManager.getLogger(CustomAuthenticationEntryPoint.class);
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        LOG.error(authException.getMessage(), authException);
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ApiErrorResponse<Void> error = ApiErrorResponse.errorMessage(
            "Authentication required"
        );
        objectMapper.writeValue(response.getWriter(), error);
    }
}
