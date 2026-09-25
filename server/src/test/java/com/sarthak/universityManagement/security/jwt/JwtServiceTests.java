package com.sarthak.universityManagement.security.jwt;

import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.testUtils.TestDataFactory;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTests extends IntegrationTests {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TestDataFactory dataFactory;
    
    @Test
    void shouldGenerateToken() {
        UserPrincipal userPrincipal = dataFactory.userPrincipal("admin", "password");
        String token = jwtService.generateToken(userPrincipal);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }
    
    @Test
    void shouldExtractUsernameFromToken() {
        UserPrincipal userPrincipal = dataFactory.userPrincipal("admin", "password");
        String token = jwtService.generateToken(userPrincipal);
        String username = jwtService.extractUsername(token);
        assertEquals("admin", username);
    }
    
    @Test
    void shouldExtractClaimsFromToken() {
        UserPrincipal userPrincipal = dataFactory.userPrincipal("admin", "password");
        String token = jwtService.generateToken(userPrincipal);
        assertEquals(userPrincipal.getUserId(), jwtService.extractUserId(token));
        assertEquals(userPrincipal.getRole(), jwtService.extractRole(token));
    }
    
    @Test
    void shouldValidateTokenForMatchingUser() {
        UserPrincipal userPrincipal = dataFactory.userPrincipal("admin", "password");
        String token = jwtService.generateToken(userPrincipal);
        assertTrue(jwtService.isValid(token, userPrincipal));
    }
    
    @Test
    void shouldRejectTokenForDifferentUser() {
        UserPrincipal userPrincipal = dataFactory.userPrincipal("admin", "password");
        String token = jwtService.generateToken(userPrincipal);
        assertFalse(jwtService.isValid(token, dataFactory.userPrincipal("student1", "password1")));
    }
    
    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        UserPrincipal userPrincipal = dataFactory.userPrincipal("admin", "password");
        String token = jwtService.generateToken(userPrincipal, 50);
        Thread.sleep(100);
        
        assertThrows(ExpiredJwtException.class, () -> jwtService.isValid(token, userPrincipal));
    }
    
    @Test
    void shouldRejectMalformedToken() {
        UserPrincipal userPrincipal = dataFactory.userPrincipal("admin", "password");
        
        assertThrows(MalformedJwtException.class, () -> jwtService.isValid("invalid.jwt.token", userPrincipal));
    }
}
