package com.sarthak.universityManagement.security;


import com.sarthak.universityManagement.courseOffering.CourseOfferingRepo;
import com.sarthak.universityManagement.enrollment.EnrollmentRepo;
import com.sarthak.universityManagement.user.CurrentUserService;
import org.springframework.stereotype.Component;

@Component(value = "authorizationService")
public class AuthorizationService {
    private final CurrentUserService currentUserService;
    private final CourseOfferingRepo courseOfferingRepo;
    private final EnrollmentRepo enrollmentRepo;

    public AuthorizationService(
        CurrentUserService currentUserService,
        CourseOfferingRepo courseOfferingRepo,
        EnrollmentRepo enrollmentRepo
    ) {
        this.currentUserService = currentUserService;
        this.courseOfferingRepo = courseOfferingRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    public boolean isInstructorForOffering(Integer offeringId) {
        var currentUser = currentUserService.getCurrentUser();
        return courseOfferingRepo.existsByIdAndInstructor_User_Id(offeringId, currentUser.getId());
    }

    public boolean isInstructorForEnrollment(Integer enrollmentId) {
        var currentUser = currentUserService.getCurrentUser();
        return enrollmentRepo.existsByIdAndCourseOffering_Instructor_User_Id(enrollmentId, currentUser.getId());
    }

    public boolean isStudentForEnrollment(Integer enrollmentId) {
        var currentUser = currentUserService.getCurrentUser();
        return enrollmentRepo.existsByIdAndStudent_User_Id(enrollmentId, currentUser.getId());
    }

}
