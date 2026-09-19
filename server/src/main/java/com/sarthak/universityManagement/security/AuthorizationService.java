package com.sarthak.universityManagement.security;


import com.sarthak.universityManagement.courseOffering.CourseOfferingRepo;
import com.sarthak.universityManagement.enrollment.EnrollmentRepo;
import com.sarthak.universityManagement.student.StudentRepo;
import com.sarthak.universityManagement.user.CurrentUserService;
import org.springframework.stereotype.Component;

@Component(value = "authorizationService")
public class AuthorizationService {
    private final CurrentUserService currentUserService;
    private final CourseOfferingRepo courseOfferingRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final StudentRepo studentRepo;

    public AuthorizationService(
        CurrentUserService currentUserService,
        CourseOfferingRepo courseOfferingRepo,
        EnrollmentRepo enrollmentRepo,
        StudentRepo studentRepo
    ) {
        this.currentUserService = currentUserService;
        this.courseOfferingRepo = courseOfferingRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.studentRepo = studentRepo;
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

    public boolean isCurrentStudent(Integer studentId) {
        var currentUser = currentUserService.getCurrentUser();
        return studentRepo.existsByIdAndUser_Id(studentId, currentUser.getId());
    }

}
