package com.sarthak.universityManagement.auth;

public final class AuthorizationExpressions {
    
    private AuthorizationExpressions() {}
    
    public static final String ADMIN =
        "hasRole('ADMIN')";
    
    public static final String INSTRUCTOR =
        "hasRole('INSTRUCTOR')";
    
    public static final String STUDENT =
        "hasRole('STUDENT')";
    
    public static final String ADMIN_OR_INSTRUCTOR =
        "hasAnyRole('ADMIN', 'INSTRUCTOR')";
    
    public static final String ANY_AUTHENTICATED =
        "isAuthenticated()";
}