package com.sarthak.universityManagement.security.jwt;

import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.testUtils.fixtures.UserFixtures;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTests extends IntegrationTests {
    @Autowired
    private JwtService jwtService;

    private static UserPrincipal principal(String username) {
        return UserFixtures.userPrincipal().userId(1).username(username).build();
    }
    
    @Test
    void shouldGenerateToken() {
        UserPrincipal userPrincipal = principal("admin");
        String token = jwtService.generateToken(userPrincipal);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }
    
    @Test
    void shouldExtractUsernameFromToken() {
        UserPrincipal userPrincipal = principal("admin");
        String token = jwtService.generateToken(userPrincipal);
        String username = jwtService.extractUsername(token);
        assertEquals("admin", username);
    }
    
    @Test
    void shouldExtractClaimsFromToken() {
        UserPrincipal userPrincipal = principal("admin");
        String token = jwtService.generateToken(userPrincipal);
        assertEquals(userPrincipal.getUserId(), jwtService.extractUserId(token));
        assertEquals(userPrincipal.getRole(), jwtService.extractRole(token));
    }
    
    @Test
    void shouldValidateTokenForMatchingUser() {
        UserPrincipal userPrincipal = principal("admin");
        String token = jwtService.generateToken(userPrincipal);
        assertTrue(jwtService.isValid(token, userPrincipal));
    }
    
    @Test
    void shouldRejectTokenForDifferentUser() {
        UserPrincipal userPrincipal = principal("admin");
        String token = jwtService.generateToken(userPrincipal);
        assertFalse(jwtService.isValid(token, principal("student1")));
    }
    
    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        UserPrincipal userPrincipal = principal("admin");
        String token = jwtService.generateToken(userPrincipal, 50);
        Thread.sleep(100);
        
        assertThrows(ExpiredJwtException.class, () -> jwtService.isValid(token, userPrincipal));
    }
    
    @Test
    void shouldRejectMalformedToken() {
        UserPrincipal userPrincipal = principal("admin");
        
        assertThrows(MalformedJwtException.class, () -> jwtService.isValid("invalid.jwt.token", userPrincipal));
    }
}
