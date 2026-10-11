package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.config.RepoTests;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.CourseOfferingFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.DepartmentFixtures;
import com.sarthak.universityManagement.testUtils.seeders.CourseOfferingSeeder;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CourseOfferingRepoTests extends RepoTests {

    @Autowired
    private CourseOfferingRepo courseOfferingRepo;

    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private SemesterSeeder semesterSeeder;
    @Autowired
    private CourseSeeder courseSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private CourseOfferingSeeder courseOfferingSeeder;

    @Nested
    class UniqueCourseOffering {

        @Test
        void shouldRejectDuplicateCourseOffering() {

            var dep = departmentSeeder.saveDefault("dep1");
            var course = courseSeeder.saveDefault(dep);
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var instructor = instructorSeeder.saveDefaultInstructor(dep);

            var offering1 = CourseOfferingFixtures.courseOffering(course, instructor, sem)
                .section("A").build();
            var offering2 = CourseOfferingFixtures.courseOffering(course, instructor, sem)
                .section("A").build();

            courseOfferingRepo.saveAndFlush(offering1);
            assertThrows(DataIntegrityViolationException.class, () -> courseOfferingRepo.saveAndFlush(offering2));
        }

        @Test
        void shouldAllowCourseOfferingWithDifferentSection() {
            var dep =  departmentSeeder.saveDefault("dep1");
            var course =  courseSeeder.saveDefault(dep);
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var instructor =  instructorSeeder.saveDefaultInstructor(dep);

            var offering1 = CourseOfferingFixtures.courseOffering(course, instructor, sem)
                .section("A").build();
            var offering2 = CourseOfferingFixtures.courseOffering(course, instructor, sem)
                .section("B").build();

            courseOfferingRepo.saveAndFlush(offering1);
            assertDoesNotThrow(() -> courseOfferingRepo.saveAndFlush(offering2));
        }

        @Test
        void shouldAllowCourseOfferingWithDifferentSemester() {
            var dep =  departmentSeeder.saveDefault("dep1");
            var course =  courseSeeder.saveDefault(dep);
            var sem1 = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var sem2 = semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2026);
            var instructor =  instructorSeeder.saveDefaultInstructor(dep);

            var offering1 = CourseOfferingFixtures.courseOffering(course, instructor, sem1)
                .section("A").build();
            var offering2 = CourseOfferingFixtures.courseOffering(course, instructor, sem2)
                .section("A").build();

            courseOfferingRepo.saveAndFlush(offering1);
            assertDoesNotThrow(() -> courseOfferingRepo.saveAndFlush(offering2));
        }

        @Test
        void shouldAllowCourseOfferingWithDifferentCourse() {
            var dep =  departmentSeeder.saveDefault("dep1");
            var course1 =  courseSeeder.saveDefault(dep);
            var course2 =  courseSeeder.saveDefault(dep);
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var instructor =  instructorSeeder.saveDefaultInstructor(dep);

            var offering1 = CourseOfferingFixtures.courseOffering(course1, instructor, sem)
                .section("A").build();
            var offering2 = CourseOfferingFixtures.courseOffering(course2, instructor, sem)
                .section("A").build();

            courseOfferingRepo.saveAndFlush(offering1);
            assertDoesNotThrow(() -> courseOfferingRepo.saveAndFlush(offering2));
        }
    }

    @Nested
    class Constraints {
        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        void shouldDenyCourseOfferingWithNonPositiveCapacity(int capacity) {
            var dep =  departmentSeeder.saveDefault("dep1");
            var course =  courseSeeder.saveDefault(dep);
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var instructor =  instructorSeeder.saveDefaultInstructor(dep);

            var offering = CourseOfferingFixtures.courseOffering(course, instructor, sem)
                .section("A")
                .capacity(capacity)
                .build();

            assertThrows(DataIntegrityViolationException.class, () -> courseOfferingRepo.saveAndFlush(offering));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 9, 10})
        void shouldAllowCourseOfferingWithCorrectEnrolledCount(int enrolledCount) {
            var dep =  departmentSeeder.saveDefault("dep1");
            var course =  courseSeeder.saveDefault(dep);
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var instructor =  instructorSeeder.saveDefaultInstructor(dep);

            var offering = CourseOfferingFixtures.courseOffering(course, instructor, sem)
                .section("A")
                .capacity(10)
                .enrolled(enrolledCount)
                .build();

            var got = courseOfferingRepo.saveAndFlush(offering);
            assertNotNull(got);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 11})
        void shouldDenyCourseOfferingWithInvalidEnrolledCount(int enrolledCount) {
            var dep =  departmentSeeder.saveDefault("dep1");
            var course =  courseSeeder.saveDefault(dep);
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var instructor =  instructorSeeder.saveDefaultInstructor(dep);

            var offering = CourseOfferingFixtures.courseOffering(course, instructor, sem)
                .section("A")
                .capacity(10)
                .enrolled(enrolledCount)
                .build();

            assertThrows(DataIntegrityViolationException.class, () -> courseOfferingRepo.saveAndFlush(offering));
        }
    }

    @Nested
    class Methods {
        @Test
        void shouldCorrectlyReturnOfferingsForSemester() {
            var dep = departmentSeeder.saveDefault("dep1");

            var course1 = courseSeeder.saveDefault(dep);
            var course2 = courseSeeder.saveDefault(dep);
            var course3 = courseSeeder.saveDefault(dep);

            var sem1 = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var sem2 = semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2026);

            var c1 = courseOfferingSeeder.saveDefault(course1, sem1, "A");
            var c2 = courseOfferingSeeder.saveDefault(course2, sem1, "A");
            var c3 = courseOfferingSeeder.saveDefault(course3, sem1, "A");
            var c4 = courseOfferingSeeder.saveDefault(course1, sem2, "A");
            var c5 = courseOfferingSeeder.saveDefault(course3, sem2, "A");

            var ret = courseOfferingRepo.search(sem1.getId(), null, null);

            assertNotNull(ret);
            assertEquals(3, ret.size());
            var ids = ret.stream().map(CourseOfferingEntity::getId).toList();
            assertTrue(ids.contains(c1.getId()));
            assertTrue(ids.contains(c2.getId()));
            assertTrue(ids.contains(c3.getId()));
        }

        @Test
        void shouldOrderBySemesterThenDepartmentNameThenCourseCodeThenSection() {
            var maths = departmentSeeder.save(DepartmentFixtures.departmentWithCode("maths").name("Mathematics").build());
            var cs = departmentSeeder.save(DepartmentFixtures.departmentWithCode("cse").name("Computer Science").build());
            var mt101 = courseSeeder.save(CourseFixtures.course(maths).code("MT101").build());
            var cs200 = courseSeeder.save(CourseFixtures.course(cs).code("CS200").build());
            var cs100 = courseSeeder.save(CourseFixtures.course(cs).code("CS100").build());

            var summer = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var winter = semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2026);
            var nextYear = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2027);

            // Saved out of order on purpose
            var last = courseOfferingSeeder.saveDefault(cs100, nextYear, "A");
            var winterCs = courseOfferingSeeder.saveDefault(cs100, winter, "A");
            var summerMaths = courseOfferingSeeder.saveDefault(mt101, summer, "A");
            var summerCs200 = courseOfferingSeeder.saveDefault(cs200, summer, "A");
            var summerCs100B = courseOfferingSeeder.saveDefault(cs100, summer, "B");
            var summerCs100A = courseOfferingSeeder.saveDefault(cs100, summer, "A");

            var ids = courseOfferingRepo.search(null, null, null).stream().map(CourseOfferingEntity::getId).toList();

            assertEquals(List.of(
                summerCs100A.getId(), summerCs100B.getId(), summerCs200.getId(), summerMaths.getId(),
                winterCs.getId(), last.getId()
            ), ids);
        }

        @Test
        void shouldCorrectlyDetermineCourseOfferingByCourseIdSemesterIdSection() {
            var dep =  departmentSeeder.saveDefault("dep1");

            var course = courseSeeder.saveDefault(dep);
            var course2 = courseSeeder.saveDefault(dep);
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
            var sem2 = semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2026);

            courseOfferingSeeder.saveDefault(course, sem, "A");

            assertTrue(courseOfferingRepo.existsByCourseIdAndSemesterIdAndSection(course.getId(), sem.getId(), "A"));
            assertFalse(courseOfferingRepo.existsByCourseIdAndSemesterIdAndSection(course.getId(), sem.getId(), "B"));
            assertFalse(courseOfferingRepo.existsByCourseIdAndSemesterIdAndSection(course.getId(), sem2.getId(), "B"));
            assertFalse(courseOfferingRepo.existsByCourseIdAndSemesterIdAndSection(course2.getId(), sem.getId(), "B"));

        }

    }

}
