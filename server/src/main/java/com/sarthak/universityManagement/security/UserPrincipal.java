package com.sarthak.universityManagement.security;

import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.user.UserEntity;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserPrincipal implements UserDetails {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private Role role;
    private boolean enabled;
    
    public UserPrincipal(UserEntity user) {
        this(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getRole(), user.isActive());
    }
    
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
    
}
