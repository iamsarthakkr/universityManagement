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

    public boolean isInstructorForCourseOffering(Integer offeringId) {
        return courseOfferingRepo.existsByIdAndInstructor_User_Id(offeringId, getCurrentUserId());
    }

    public boolean isInstructorForEnrollment(Integer enrollmentId) {
        return enrollmentRepo.existsByIdAndCourseOffering_Instructor_User_Id(enrollmentId, getCurrentUserId());
    }

    public boolean isStudentForEnrollment(Integer enrollmentId) {
        return enrollmentRepo.existsByIdAndStudent_User_Id(enrollmentId, getCurrentUserId());
    }

    public boolean isCurrentStudent(Integer studentId) {
        return studentRepo.existsByIdAndUser_Id(studentId, getCurrentUserId());
    }

    private Integer getCurrentUserId() {
        return currentUserService.getCurrentUserId();
    }

}
