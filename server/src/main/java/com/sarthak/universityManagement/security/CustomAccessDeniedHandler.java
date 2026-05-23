package com.sarthak.universityManagement.security;

import com.sarthak.universityManagement.common.rest.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger LOG = LogManager.getLogger(CustomAccessDeniedHandler.class);
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        LOG.error(accessDeniedException.getMessage(), accessDeniedException);
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse<Void> error = ApiErrorResponse.errorMessage(
            "Access denied"
        );
        objectMapper.writeValue(response.getWriter(), error);
    }
}
