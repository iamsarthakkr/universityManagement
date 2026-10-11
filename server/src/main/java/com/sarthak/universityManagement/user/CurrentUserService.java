package com.sarthak.universityManagement.user;

import com.sarthak.universityManagement.common.exceptions.ForbiddenException;
import com.sarthak.universityManagement.common.types.Role;
import com.sarthak.universityManagement.department.DepartmentMapper;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.instructor.InstructorService;
import com.sarthak.universityManagement.security.UserPrincipal;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentService;
import com.sarthak.universityManagement.user.dto.MeResponse;
import com.sarthak.universityManagement.user.dto.Profile;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserService userService;
    private final StudentService studentService;
    private final InstructorService instructorService;
    
    public UserEntity getCurrentUser() {
        return userService.getUserById(getCurrentUserId());
    }

    @Transactional(readOnly = true)
    public MeResponse getMe(UserPrincipal userPrincipal) {
        return UserMapper.toMe(userPrincipal, getUserProfile(userPrincipal));
    }

    public UserPrincipal getCurrentUserPrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
    }

    @Transactional(readOnly = true)
    public StudentEntity getCurrentStudent() {
        var user = getCurrentUser();
        if(user.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Current user is not a student");
        }
        return studentService.getStudentByUserId(getCurrentUserId());
    }

    @Transactional(readOnly = true)
    public InstructorEntity getCurrentInstructor() {
        var user = getCurrentUser();
        if(user.getRole() != Role.INSTRUCTOR) {
            throw new ForbiddenException("Current user is not an instructor");
        }
        return instructorService.getInstructorByUserId(getCurrentUserId());
    }

    public Integer getCurrentUserId() {
        return getCurrentUserPrincipal().getId();
    }

    public Role getCurrentUserRole() {
        return getCurrentUserPrincipal().getRole();
    }

    /* ------------------------------------------------------ helpers ------------------------------------------------ */

    @Nullable
    private Profile getUserProfile(UserPrincipal userPrincipal) {
        return switch (userPrincipal.getRole()) {
            case ADMIN -> null;
            case STUDENT -> {
                var student = studentService.getStudentByUserId(userPrincipal.getId());
                yield new Profile(student.getId(), student.getFirstName(), student.getLastName(), DepartmentMapper.toResponse(student.getDepartment()));
            }
            case INSTRUCTOR -> {
                var instructor = instructorService.getInstructorByUserId(userPrincipal.getId());
                yield new Profile(instructor.getId(), instructor.getFirstName(), instructor.getLastName(), DepartmentMapper.toResponse(instructor.getDepartment()));
            }
        };
    }
}
