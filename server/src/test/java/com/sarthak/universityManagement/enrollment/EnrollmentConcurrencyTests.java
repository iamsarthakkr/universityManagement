package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.config.TestClockConfig;
import com.sarthak.universityManagement.config.TestUtilsConfiguration;
import com.sarthak.universityManagement.course.CourseRepo;
import com.sarthak.universityManagement.courseOffering.CourseOfferingRepo;
import com.sarthak.universityManagement.courseOffering.CourseOfferingService;
import com.sarthak.universityManagement.department.DepartmentRepo;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.semester.SemesterRepo;
import com.sarthak.universityManagement.student.StudentRepo;
import com.sarthak.universityManagement.testUtils.scenerio.courseOffering.CourseOfferingScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.student.StudentScenarioSeeder;
import com.sarthak.universityManagement.testUtils.security.TestAuthentication;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestUtilsConfiguration.class, TestClockConfig.class})
public class EnrollmentConcurrencyTests {
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private CourseOfferingService courseOfferingService;

    @Autowired
    private EnrollmentRepo enrollmentRepo;
    @Autowired
    private CourseOfferingRepo courseOfferingRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private InstructorRepo instructorRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private DepartmentRepo departmentRepo;
    @Autowired
    private SemesterRepo semesterRepo;

    @Autowired
    private StudentScenarioSeeder studentScenarioSeeder;
    @Autowired
    private CourseOfferingScenarioSeeder courseOfferingScenarioSeeder;
    @Autowired
    private Clock clock;

    @AfterEach
    public void tearDown() {
        enrollmentRepo.deleteAll();
        courseOfferingRepo.deleteAll();
        courseRepo.deleteAll();
        instructorRepo.deleteAll();
        studentRepo.deleteAll();
        userRepo.deleteAll();
        semesterRepo.deleteAll();
        departmentRepo.deleteAll();
    }

    @Test
    void shouldAllowOnlyOneEnrollmentWhenOneSeatRemains() throws InterruptedException, ExecutionException {
        var offeringScenario = courseOfferingScenarioSeeder.builder()
            .capacity(1)
            .enrolled(0)
            .semester(s -> s.registrationOpenOn(LocalDate.now(clock)))
            .build();
        var studentScenario1 = studentScenarioSeeder.builder().studentNumber(1).build();
        var studentScenario2 = studentScenarioSeeder.builder().studentNumber(2).build();

        var student1 = studentScenario1.student();
        var student2 = studentScenario2.student();
        var offering = offeringScenario.courseOffering();
        var instructor = offering.getInstructor();

        TestAuthentication.asStudent(student1);
        var enrollment1 = enrollmentService.createEnrollment(student1.getId(), offering.getId());
        TestAuthentication.asStudent(student2);
        var enrollment2 = enrollmentService.createEnrollment(student2.getId(), offering.getId());
        TestAuthentication.clear();

        var executor = Executors.newFixedThreadPool(2);

        var ready = new CountDownLatch(2);
        var start =  new CountDownLatch(1);

        Callable<Throwable> approveFirst = () -> {
            TestAuthentication.asInstructor(instructor);
            ready.countDown();
            start.await();

            try {
                enrollmentService.approveEnrollment(enrollment1.id());
                return null;
            } catch (Throwable throwable) {
                return throwable;
            }
            finally {
                TestAuthentication.clear();
            }
        };

        Callable<Throwable> approveSecond = () -> {
            TestAuthentication.asInstructor(instructor);
            ready.countDown();
            start.await();

            try {
                enrollmentService.approveEnrollment(enrollment2.id());
                return null;
            } catch (Throwable throwable) {
                return throwable;
            }
            finally {
                TestAuthentication.clear();
            }
        };

        var future1 = executor.submit(approveFirst);
        var future2 = executor.submit(approveSecond);

        ready.await();
        start.countDown();


        future1.get();
        future2.get();

        executor.shutdown();


        TestAuthentication.asInstructor(instructor);

        var approvedCount = 0;
        var enrollments = enrollmentService.getEnrollmentsForCourseOffering(offering.getId());
        for (var enrollment : enrollments) {
            if(enrollment.enrollmentStatus().equals(EnrollmentStatus.ENROLLED)) approvedCount++;
        }
        assertEquals(1, approvedCount);

        var updatedOffering = courseOfferingService.getCourseOfferingEntity(offering.getId());
        assertEquals(1,  updatedOffering.getEnrolled());

        TestAuthentication.clear();
    }
}
