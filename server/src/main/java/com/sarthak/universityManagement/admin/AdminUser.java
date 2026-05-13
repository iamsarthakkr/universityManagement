package com.sarthak.universityManagement.admin;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AdminUser {
    @Value("${app.admin.username}")
    private String username;
    
    @Value("${app.admin.password}")
    private String password;
    
    @Value("${app.admin.email}")
    private String email;
}
