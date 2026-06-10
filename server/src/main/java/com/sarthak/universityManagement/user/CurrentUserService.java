package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.common.exceptions.ForbiddenException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.instructor.InstructorService;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserService userService;
    private final StudentService studentService;
    private final InstructorService instructorService;
    
    public UserEntity getCurrentUser() {
        return userService.getUserById(getCurrentUserId());
    }
    
    public StudentEntity getCurrentStudent() {
        var user = getCurrentUser();
        if(user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Current user is not a student");
        }
        return studentService.getStudentByUserId(getCurrentUserId());
    }
    
    public InstructorEntity getCurrentInstructor() {
        var user = getCurrentUser();
        if(user.getRole() != Role.INSTRUCTOR) {
            throw new ForbiddenException("Current user is not an instructor");
        }
        return instructorService.getInstructorByUserId(getCurrentUserId());
    }
    
    public Integer getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated") { };
        }
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        return Objects.requireNonNull(userPrincipal, "User principal is null").getUserId();
    }
}
