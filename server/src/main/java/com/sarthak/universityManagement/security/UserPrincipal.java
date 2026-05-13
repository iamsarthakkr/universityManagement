package com.sarthak.universityManagement.security;

import com.sarthak.universityManagement.common.types.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    private Integer userId;
    private String username;
    private String password;
    private Role role;
    private boolean enabled;
    
    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public @Nullable String getPassword() {
        return this.password;
    }
    
    @Override
    @NonNull
    public String getUsername() {
        return this.username;
    }
    
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public Role getRole() {
        return role;
    }
    
    public Integer getUserId() {
        return this.userId;
    }
}
