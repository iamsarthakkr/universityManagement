package com.sarthak.universityManagement.auth;

import com.sarthak.universityManagement.auth.dto.LoginRequest;
import com.sarthak.universityManagement.auth.dto.LoginResponse;
import com.sarthak.universityManagement.common.rest.ApiResponse;
import com.sarthak.universityManagement.common.rest.Res;
import com.sarthak.universityManagement.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return Res.success("Login successful", authService.login(loginRequest));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return Res.success(authService.me());
    }
    
}
