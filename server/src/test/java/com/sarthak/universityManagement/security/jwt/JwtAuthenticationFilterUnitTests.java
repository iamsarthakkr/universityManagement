package com.sarthak.universityManagement.security.jwt;

import com.sarthak.universityManagement.common.rest.ApiErrorResponse;
import com.sarthak.universityManagement.security.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.RequestMatcher;
import tools.jackson.databind.ObjectMapper;

import java.io.PrintWriter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterUnitTests {
    @Mock
    JwtService jwtService;
    @Mock
    UserDetailsService userDetailsService;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    RequestMatcher publicEndpoints;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;
    @InjectMocks
    private JwtAuthenticationFilter filter;
    
    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void shouldNotFilterPublicEndpoints() {
        when(publicEndpoints.matches(request)).thenReturn(true);
        
        assertTrue(filter.shouldNotFilter(request));
    }
    
    @Test
    void shouldFilterProtectedEndpoints() {
        when(publicEndpoints.matches(request)).thenReturn(false);
        assertFalse(filter.shouldNotFilter(request));
    }
    
    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderMissing() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        
        filter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsNotBearer() throws Exception {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic ABC");
        
        filter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void shouldSetAuthenticationWhenJwtIsValid() throws Exception {
        String token = "valid-token";
        String username = "user1";
        UserDetails userDetails = User
            .withUsername(username)
            .password("password")
            .roles("STUDENT")
            .build();
        
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.isValid(token, userDetails)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        
        filter.doFilterInternal(request, response, filterChain);
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(username, authentication.getName());
        
        verify(filterChain).doFilter(request, response);
    }
    
    
    @Test
    void shouldNotSetAuthenticationWhenJwtIsInvalid() throws Exception {
        String token = "invalid-token";
        String username = "user1";
        UserDetails userDetails = User
            .withUsername(username)
            .password("password")
            .roles("STUDENT")
            .build();
        
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(jwtService.isValid(token, userDetails)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        
        filter.doFilterInternal(request, response, filterChain);
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        assertNull(authentication);
        verify(filterChain).doFilter(request, response);
    }
    
    
    @Test
    void shouldWriteUnauthorizedResponseWhenTokenExpired() throws Exception {
        String token = "expired-token";
        PrintWriter writer = mock(PrintWriter.class);
        
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenThrow(mock(ExpiredJwtException.class));
        when(response.getWriter()).thenReturn(writer);
        
        filter.doFilterInternal(request, response, filterChain);
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        assertNull(authentication);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValue(eq(writer), any(ApiErrorResponse.class));
        verify(filterChain, never()).doFilter(request, response);
    }
    
    
    @Test
    void shouldWriteUnauthorizedResponseWhenTokenUnsupported() throws Exception {
        String token = "unsupported-token";
        PrintWriter writer = mock(PrintWriter.class);
        
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenThrow(new UnsupportedJwtException("Unsupported jwt token"));
        when(response.getWriter()).thenReturn(writer);
        
        filter.doFilterInternal(request, response, filterChain);
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        assertNull(authentication);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValue(eq(writer), any(ApiErrorResponse.class));
        verify(filterChain, never()).doFilter(request, response);
    }
    
    @Test
    void shouldWriteUnauthorizedResponseWhenTokenInvalid() throws Exception {
        String token = "invalid-token";
        PrintWriter writer = mock(PrintWriter.class);
        
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenThrow(new JwtException("Invalid token"));
        when(response.getWriter()).thenReturn(writer);
        
        filter.doFilterInternal(request, response, filterChain);
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        assertNull(authentication);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(objectMapper).writeValue(eq(writer), any(ApiErrorResponse.class));
        verify(filterChain, never()).doFilter(request, response);
    }
}

