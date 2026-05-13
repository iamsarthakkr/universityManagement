package com.sarthak.universityManagement.security;

import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.user.UserEntity;
import com.sarthak.universityManagement.user.UserRepo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepo userRepo;
    
    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity user = userRepo
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username " + username));
        
        return UserPrincipal
            .builder()
            .userId(user.getId())
            .username(user.getUsername())
            .password(user.getPassword())
            .role(user.getRole())
            .enabled(user.isActive())
            .build();
    }
}
