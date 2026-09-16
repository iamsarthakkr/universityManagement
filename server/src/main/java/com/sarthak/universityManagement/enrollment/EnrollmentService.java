package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.auth.AuthorizationExpressions;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingService;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.enrollment.validators.EnrollmentValidator;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EnrollmentService {
    private final EnrollmentRepo enrollmentRepo;
    private final CurrentUserService currentUserService;
    private final CourseOfferingService courseOfferingService;

    @Autowired
    public EnrollmentService(
        EnrollmentRepo enrollmentRepo,
        CurrentUserService currentUserService,
        CourseOfferingService courseOfferingService
    ) {
        this.enrollmentRepo = enrollmentRepo;
        this.currentUserService = currentUserService;
        this.courseOfferingService = courseOfferingService;
    }

    @PreAuthorize(AuthorizationExpressions.STUDENT)
    public EnrollmentResponse createEnrollment(Integer courseOfferingId) {
        var student = currentUserService.getCurrentStudent();
        var courseOffering = courseOfferingService.getCourOfferingEntity(courseOfferingId);

        validateEnrollmentRequest(courseOffering, student);

        EnrollmentEntity toSave = EnrollmentEntity.builder()
            .student(student)
            .courseOffering(courseOffering)
            .status(EnrollmentStatus.PENDING)
            .build();

        return EnrollmentMapper.toResponse(enrollmentRepo.save(toSave));
    }


    private void validateEnrollmentRequest(CourseOfferingEntity courseOffering, StudentEntity student) {
        EnrollmentValidator.validateUniqueEnrollment(enrollmentRepo, student, courseOffering);
        EnrollmentValidator.validateOffering(courseOffering);
    }


}
