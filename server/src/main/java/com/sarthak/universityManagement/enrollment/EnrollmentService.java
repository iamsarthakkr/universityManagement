package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingService;
import com.sarthak.universityManagement.enrollment.dto.EnrollmentResponse;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.security.annotation.AdminOrCourseOfferingInstructor;
import com.sarthak.universityManagement.security.annotation.AdminOrEnrollmentInstructor;
import com.sarthak.universityManagement.security.annotation.CanAccessEnrollment;
import com.sarthak.universityManagement.security.annotation.CurrentStudent;
import com.sarthak.universityManagement.security.annotation.EnrollmentStudent;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.student.StudentService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class EnrollmentService {
    private final EnrollmentRepo enrollmentRepo;
    private final CourseOfferingService courseOfferingService;
    private final StudentService studentService;
    private final Clock clock;

    @Autowired
    public EnrollmentService(
        EnrollmentRepo enrollmentRepo,
        CourseOfferingService courseOfferingService,
        StudentService studentService,
        Clock clock
    ) {
        this.enrollmentRepo = enrollmentRepo;
        this.courseOfferingService = courseOfferingService;
        this.studentService = studentService;
        this.clock = clock;
    }

    @CanAccessEnrollment
    public EnrollmentResponse getEnrollment(Integer enrollmentId) {
        var enrollment = getEnrollmentOrThrow(enrollmentId);
        return EnrollmentMapper.toResponse(enrollment, LocalDate.now(clock));
    }

    @CurrentStudent
    public List<EnrollmentResponse> getEnrollmentsForStudent(Integer studentId, @Nullable Integer semesterId, @Nullable EnrollmentStatus enrollmentStatus) {
        return enrollmentRepo
            .findByStudentId(studentId, semesterId, enrollmentStatus)
            .stream()
            .map(e -> EnrollmentMapper.toResponse(e, LocalDate.now(clock)))
            .toList();
    }

    @AdminOrCourseOfferingInstructor
    public List<EnrollmentResponse> getEnrollmentsForCourseOffering(Integer courseOfferingId, @Nullable EnrollmentStatus status) {
        return enrollmentRepo
            .findAllForCourseOfferingWithDetails(courseOfferingId, status)
            .stream()
            .map(e -> EnrollmentMapper.toResponse(e, LocalDate.now(clock)))
            .toList();
    }

    @CurrentStudent
    public EnrollmentResponse createEnrollment(Integer studentId, Integer courseOfferingId) {
        var student = studentService.getStudentEntity(studentId);
        var courseOffering = courseOfferingService.getCourseOfferingEntity(courseOfferingId);

        validateEnrollmentRequest(courseOffering, student);

        EnrollmentEntity toSave = EnrollmentEntity.builder()
            .student(student)
            .courseOffering(courseOffering)
            .status(EnrollmentStatus.PENDING)
            .build();

        return EnrollmentMapper.toResponse(enrollmentRepo.save(toSave), LocalDate.now(clock));
    }

    @AdminOrEnrollmentInstructor
    public EnrollmentResponse approveEnrollment(Integer enrollmentId) {
        return getEnrollmentResponse(updateEnrollment(enrollmentId, EnrollmentStatus.ENROLLED));
    }

    @AdminOrEnrollmentInstructor
    public EnrollmentResponse rejectEnrollment(Integer enrollmentId) {
        return getEnrollmentResponse(updateEnrollment(enrollmentId, EnrollmentStatus.REJECTED));
    }

    @EnrollmentStudent
    public EnrollmentResponse cancelEnrollment(Integer enrollmentId) {
        return getEnrollmentResponse(updateEnrollment(enrollmentId, EnrollmentStatus.CANCELLED));
    }

    @EnrollmentStudent
    public EnrollmentResponse dropEnrollment(Integer enrollmentId) {
        return getEnrollmentResponse(updateEnrollment(enrollmentId, EnrollmentStatus.DROPPED));
    }

    /* ---------------------------------------------------------------------------------------------------------------*/

    private void validateEnrollmentRequest(CourseOfferingEntity courseOffering, StudentEntity student) {
        if(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student.getId(), courseOffering.getId())) {
            throw new BadRequestException("Enrollment already exists for student in the course offering");
        }
        var semester = courseOffering.getSemester();
        if(!semester.isRegistrationOpen(LocalDate.now(clock))) {
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

    private EnrollmentEntity updateEnrollment(Integer enrollmentId, EnrollmentStatus targetStatus) {
        var enrollment = getEnrollmentForUpdateOrThrow(enrollmentId);
        if(!enrollment.canTransitionTo(targetStatus)) {
            throw new BadRequestException("Enrollment with id " + enrollmentId + " cannot be " + targetStatus.toString().toLowerCase(Locale.ROOT));
        }
        var courseOffering = courseOfferingService.getCourseOfferingForEnrollment(enrollment.getCourseOffering().getId());
        if(EnrollmentStatus.ENROLLED.equals(targetStatus)) {
            courseOffering.enroll();
        }
        if(EnrollmentStatus.DROPPED.equals(targetStatus)) {
            courseOffering.releaseEnrolled();
        }
        enrollment.setStatus(targetStatus);

        return enrollmentRepo.saveAndFlush(enrollment);
    }

    private EnrollmentResponse getEnrollmentResponse(EnrollmentEntity enrollment) {
        return EnrollmentMapper.toResponse(enrollment, LocalDate.now(clock));
    }

}
