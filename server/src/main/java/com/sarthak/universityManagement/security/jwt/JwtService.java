package com.sarthak.universityManagement.security.jwt;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;
    
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;
    
    private final String USER_ID = "userId";
    private final String ROLE = "ROLE";
    
    public String generateToken(UserPrincipal authenticatedUser) {
        return generateToken(authenticatedUser, expirationMs);
    }
    
    String generateToken(UserPrincipal authenticatedUser, long exp) {
        Instant now = Instant.now();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID, authenticatedUser.getUserId());
        claims.put(ROLE, authenticatedUser.getRole().name());
        
        return Jwts.builder()
            .claims(claims)
            .subject(authenticatedUser.getUsername())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(exp)))
            .signWith(getSigningKey())
            .compact();
    }
    
    public String extractUsername(String authToken) {
        return extractClaims(authToken).getSubject();
    }
    
    public Integer extractUserId(String authToken) {
        return extractClaims(authToken).get(USER_ID, Integer.class);
    }
    
    public Role extractRole(String authToken) {
        String roleString =  extractClaims(authToken).get(ROLE, String.class);
        return Role.valueOf(roleString);
    }
    
    public boolean isValid(String authToken, UserDetails userDetails) {
        String username = extractUsername(authToken);
        return username.equals(userDetails.getUsername()) && !isExpired(authToken);
    }
    
    private boolean isExpired(String authToken) {
        return extractClaims(authToken)
            .getExpiration()
            .before(new Date());
    }
    
    private Claims extractClaims(String authToken) {
        return Jwts
            .parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(authToken)
            .getPayload();
    
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}