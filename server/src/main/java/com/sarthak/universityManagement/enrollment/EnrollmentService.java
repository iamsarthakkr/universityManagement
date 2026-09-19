package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingService;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.security.annotation.AdminOrCourseOfferingInstructor;
import com.sarthak.universityManagement.security.annotation.AdminOrEnrollmentInstructor;
import com.sarthak.universityManagement.security.annotation.CurrentStudent;
import com.sarthak.universityManagement.security.annotation.EnrollmentStudent;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EnrollmentService {
    private final EnrollmentRepo enrollmentRepo;
    private final CourseOfferingService courseOfferingService;
    private final StudentService studentService;

    @Autowired
    public EnrollmentService(
        EnrollmentRepo enrollmentRepo,
        CourseOfferingService courseOfferingService,
        StudentService studentService
    ) {
        this.enrollmentRepo = enrollmentRepo;
        this.courseOfferingService = courseOfferingService;
        this.studentService = studentService;
    }

    @CurrentStudent
    public EnrollmentResponse createEnrollment(Integer studentId, Integer courseOfferingId) {
        var student = studentService.getStudentEntity(studentId);
        var courseOffering = courseOfferingService.getCourOfferingEntity(courseOfferingId);

        validateEnrollmentRequest(courseOffering, student);

        EnrollmentEntity toSave = EnrollmentEntity.builder()
            .student(student)
            .courseOffering(courseOffering)
            .status(EnrollmentStatus.PENDING)
            .build();

        return EnrollmentMapper.toResponse(enrollmentRepo.save(toSave));
    }

    @AdminOrEnrollmentInstructor
    public void approveEnrollment(Integer enrollmentId) {
        var enrollment = getEnrollmentForUpdateOrThrow(enrollmentId);
        if(!enrollment.canTransitionTo(EnrollmentStatus.ENROLLED)) {
            throw new BadRequestException("Enrollment with id " + enrollmentId + " cannot be approved");
        }

        var courseOffering = courseOfferingService.getCourseOfferingForEnrollment(enrollment.getCourseOffering().getId());
        courseOffering.enroll();
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
    }

    @AdminOrEnrollmentInstructor
    public void rejectEnrollment(Integer enrollmentId) {
        var enrollment = getEnrollmentOrThrow(enrollmentId);
        if(!enrollment.canTransitionTo(EnrollmentStatus.REJECTED)) {
            throw new BadRequestException("Enrollment with id " + enrollmentId + " cannot be rejected");
        }
        enrollment.setStatus(EnrollmentStatus.REJECTED);
    }

    @EnrollmentStudent
    public void cancelEnrollment(Integer enrollmentId) {
        var enrollment = getEnrollmentOrThrow(enrollmentId);
        if(!enrollment.canTransitionTo(EnrollmentStatus.CANCELLED)) {
            throw new BadRequestException("Enrollment with id " + enrollmentId + " cannot be cancelled");
        }
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
    }

    @EnrollmentStudent
    public void dropEnrollment(Integer enrollmentId) {
        var enrollment = getEnrollmentForUpdateOrThrow(enrollmentId);
        if(!enrollment.canTransitionTo(EnrollmentStatus.DROPPED)) {
            throw new BadRequestException("Enrollment with id " + enrollmentId + " cannot be dropped");
        }
        var courseOffering = courseOfferingService.getCourseOfferingForEnrollment(enrollment.getCourseOffering().getId());
        courseOffering.releaseEnrolled();
        enrollment.setStatus(EnrollmentStatus.DROPPED);
    }

    @CurrentStudent
    public List<EnrollmentResponse> getEnrollmentsForStudent(Integer studentId) {
        return enrollmentRepo
            .findByStudentId(studentId)
            .stream()
            .map(EnrollmentMapper::toResponse)
            .toList();
    }

    @AdminOrCourseOfferingInstructor
    public List<EnrollmentResponse> getEnrollmentsForCourseOffering(Integer courseOfferingId) {
        return enrollmentRepo
            .findByCourseOfferingId(courseOfferingId)
            .stream()
            .map(EnrollmentMapper::toResponse)
            .toList();
    }

    /* ---------------------------------------------------------------------------------------------------------------*/

    private void validateEnrollmentRequest(CourseOfferingEntity courseOffering, StudentEntity student) {
        if(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student.getId(), courseOffering.getId())) {
            throw new BadRequestException("Enrollment already exists for student in the course offering");
        }
        var semester = courseOffering.getSemester();
        if(!semester.isRegistrationOpen(LocalDate.now())) {
            throw new BadRequestException("Registration is not open");
        }
    }

    private EnrollmentEntity getEnrollmentOrThrow(Integer enrollmentId) {
        return enrollmentRepo
            .findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + enrollmentId + " not found"));
    }

    private EnrollmentEntity getEnrollmentForUpdateOrThrow(Integer enrollmentId) {
        return enrollmentRepo
            .findForUpdateById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment with id " + enrollmentId + " not found"));
    }

}
