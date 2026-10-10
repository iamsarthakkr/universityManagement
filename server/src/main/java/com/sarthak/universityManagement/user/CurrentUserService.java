package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.common.exceptions.ForbiddenException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.instructor.InstructorService;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserService userService;
    private final StudentService studentService;
    private final InstructorService instructorService;
    
    public UserEntity getCurrentUser() {
        return userService.getUserById(getCurrentUserId());
    }

    public UserPrincipal getCurrentUserPrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
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
        return getCurrentUserPrincipal().getUserId();
    }

    public Role getCurrentUserRole() {
        return getCurrentUserPrincipal().getRole();
    }
}
