package com.sarthak.universityManagement.enrollment;

import com.sarthak.universityManagement.config.RepoTests;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.student.StudentEntity;
import com.sarthak.universityManagement.testUtils.fixtures.EnrollmentFixtures;
import com.sarthak.universityManagement.testUtils.scenerio.courseOffering.CourseOfferingScenarioSeeder;
import com.sarthak.universityManagement.testUtils.scenerio.enrollment.EnrollmentScenarioSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentRepoTests extends RepoTests {

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private EnrollmentScenarioSeeder enrollmentScenarioSeeder;

    @Autowired
    private CourseOfferingScenarioSeeder courseOfferingScenarioSeeder;

    StudentEntity student1, student2;
    CourseOfferingEntity courseOffering1, courseOffering2;
    EnrollmentEntity enrollment1, enrollment2, enrollment3;

    @BeforeEach
    public void setup() {
        var e1 = enrollmentScenarioSeeder.builder()
            .departmentNumber(1)
            .student(s -> s.studentNumber(1))
            .offering(o -> o.courseNumber(1).instructorNumber(1))
            .build();

        var e2 = enrollmentScenarioSeeder.builder()
            .departmentNumber(1)
            .student(s -> s.studentNumber(2))
            .offering(o -> o.courseNumber(1).instructorNumber(1))
            .build();


        var e3 = enrollmentScenarioSeeder.builder()
            .departmentNumber(1)
            .student(s -> s.studentNumber(1))
            .offering(o -> o.courseNumber(2).instructorNumber(2))
            .build();

        student1 = e1.student();
        student2 = e2.student();

        courseOffering1 = e1.courseOffering();
        courseOffering2 = e3.courseOffering();

        enrollment1 = e1.enrollment();
        enrollment2 = e2.enrollment();
        enrollment3 = e3.enrollment();
    }

    @Test
    void shouldFetchEnrollmentsForStudent() {
        var res1 = enrollmentRepo.findByStudentId(student1.getId());
        var res2 = enrollmentRepo.findByStudentId(student2.getId());

        assertEquals(2, res1.size());
        assertEquals(1, res2.size());

        assertTrue(res1.contains(enrollment1));
        assertTrue(res1.contains(enrollment3));
        assertTrue(res2.contains(enrollment2));
    }

    @Test
    void shouldFetchEnrollmentsForCourseOffering() {
        var res1 =  enrollmentRepo.findByCourseOfferingId(courseOffering1.getId());
        var res2 =  enrollmentRepo.findByCourseOfferingId(courseOffering2.getId());

        assertEquals(2, res1.size());
        assertEquals(1, res2.size());
        assertTrue(res1.contains(enrollment1));
        assertTrue(res1.contains(enrollment2));
        assertTrue(res2.contains(enrollment3));

    }

    @Test
    void shouldDetermineEnrollmentByStudentAndOffering() {
        assertTrue(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student1.getId(), courseOffering1.getId()));
        assertTrue(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student1.getId(), courseOffering2.getId()));
        assertTrue(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student2.getId(), courseOffering1.getId()));

        assertFalse(enrollmentRepo.existsByStudentIdAndCourseOfferingId(student2.getId(), courseOffering2.getId()));
    }

    @Test
    void shouldDetermineEnrollmentByIdAndInstructor() {
        var instructor1 = courseOffering1.getInstructor();
        var instructor2 = courseOffering2.getInstructor();

        assertTrue(enrollmentRepo.existsByIdAndCourseOffering_Instructor_User_Id(enrollment1.getId(), instructor1.getUser().getId()));
        assertTrue(enrollmentRepo.existsByIdAndCourseOffering_Instructor_User_Id(enrollment2.getId(), instructor1.getUser().getId()));

        assertFalse(enrollmentRepo.existsByIdAndCourseOffering_Instructor_User_Id(enrollment2.getId(), instructor2.getUser().getId()));

    }

    @Test
    void shouldRejectDuplicateEnrollment() {
        var toSave = EnrollmentFixtures.enrollment(student1, courseOffering1);
        assertThrows(DataIntegrityViolationException.class, () -> enrollmentRepo.saveAndFlush(toSave.build()));
    }


}
