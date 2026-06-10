package com.sarthak.universityManagement.testUtils;

import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.user.UserEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class TestSecurityUtils {
    public static void authenticateAs(UserEntity user) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
            
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    
    }
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
