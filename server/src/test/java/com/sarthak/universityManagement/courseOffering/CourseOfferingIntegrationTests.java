package com.sarthak.universityManagement.courseOffering;

import com.sarthak.universityManagement.common.exceptions.BadRequestException;
import com.sarthak.universityManagement.common.exceptions.ResourceNotFoundException;
import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.courseOffering.dto.CourseOfferingResponse;
import com.sarthak.universityManagement.department.DepartmentEntity;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.CourseOfferingFixtures;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import com.sarthak.universityManagement.testUtils.security.WithAdmin;
import com.sarthak.universityManagement.testUtils.seeders.CourseOfferingSeeder;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import({
    CourseSeeder.class,
    InstructorSeeder.class,
    DepartmentSeeder.class,
    SemesterSeeder.class,
    CourseOfferingSeeder.class
})
public class CourseOfferingIntegrationTests {
    @Autowired
    private CourseOfferingService courseOfferingService;

    @Autowired
    private CourseSeeder courseSeeder;
    @Autowired
    private SemesterSeeder semesterSeeder;
    @Autowired
    private InstructorSeeder instructorSeeder;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private CourseOfferingSeeder courseOfferingSeeder;

    private DepartmentEntity department;
    private SemesterEntity semester;
    private InstructorEntity instructor;
    private CourseEntity course;

    @BeforeEach
    void setUp() {
        department = departmentSeeder.saveDefault("dep");
        course = courseSeeder.saveDefault(department);
        instructor = instructorSeeder.saveDefaultInstructor(department);
        semester = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2026);
    }

    @Nested
    @WithAdmin
    class Creation {

        @Test
        void shouldCreateCourseOfferingSuccessfully() {
            var courseOfferingReq = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(course.getId())
                .semesterId(semester.getId())
                .instructorId(instructor.getId())
                .build();

            var ret = courseOfferingService.createOffering(courseOfferingReq);

            assertNotNull(ret);
            assertEquals("A", ret.section());
            assertEquals(100, ret.capacity());
            assertEquals(course.getId(), ret.courseId());
            assertEquals(semester.getId(), ret.semesterId());
            assertEquals(instructor.getId(), ret.instructorId());
        }

        @Test
        void shouldThrowWhenCourseDoesNotExist() {
            var courseOfferingReq = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(-1)
                .semesterId(semester.getId())
                .instructorId(instructor.getId())
                .build();

            assertThrows(ResourceNotFoundException.class, () -> courseOfferingService.createOffering(courseOfferingReq));
        }

        @Test
        void shouldThrowWhenSemesterDoesNotExist() {
            var courseOfferingReq = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(course.getId())
                .semesterId(-1)
                .instructorId(instructor.getId())
                .build();

            assertThrows(ResourceNotFoundException.class, () -> courseOfferingService.createOffering(courseOfferingReq));
        }

        @Test
        void shouldThrowWhenInstructorDoesNotExist() {
            var courseOfferingReq = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(course.getId())
                .semesterId(semester.getId())
                .instructorId(-1)
                .build();

            assertThrows(ResourceNotFoundException.class, () -> courseOfferingService.createOffering(courseOfferingReq));
        }

        @Test
        void shouldThrowWhenInstructorDoesNotMatchCourseDepartment() {
            var dep2 = departmentSeeder.saveDefault("dep2");
            var instructor2 = instructorSeeder.saveDefaultInstructor(dep2);

            var courseOfferingReq = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(course.getId())
                .semesterId(semester.getId())
                .instructorId(instructor2.getId())
                .build();

            assertThrows(BadRequestException.class, () -> courseOfferingService.createOffering(courseOfferingReq));
        }


        @ParameterizedTest
        @CsvSource({"ACTIVE", "COMPLETED", "CANCELLED"})
        void shouldThrowWhenSemesterIsNotPlanned(SemesterStatus semesterStatus) {
            var sem2 = semesterSeeder.saveSemester(
                SemesterFixtures.semester()
                    .year(2027)
                    .term(SemesterTerm.SUMMER)
                    .status(semesterStatus)
                    .build()
            );

            var courseOfferingReq = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(course.getId())
                .semesterId(sem2.getId())
                .instructorId(instructor.getId())
                .build();

            assertThrows(BadRequestException.class, () -> courseOfferingService.createOffering(courseOfferingReq));
        }

        @Test
        void shouldThrowOnDuplicateCourseOffering() {
            var courseOfferingReq1 = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(course.getId())
                .semesterId(semester.getId())
                .instructorId(instructor.getId())
                .build();

            courseOfferingService.createOffering(courseOfferingReq1);

            var instructor2 = instructorSeeder.saveDefaultInstructor(department);

            var courseOfferingReq2 = CourseOfferingFixtures.courseOfferingRequest()
                .capacity(100)
                .section("A")
                .courseId(course.getId())
                .semesterId(semester.getId())
                .instructorId(instructor2.getId())
                .build();

            assertThrows(BadRequestException.class, () -> courseOfferingService.createOffering(courseOfferingReq2));
        }

   }

    @Nested
    class Retrieval {

        @Test
        void shouldReturnCorrectResponse() {
            var offering = courseOfferingSeeder.saveDefault(course, semester, "A");

            var ret = courseOfferingService.getOffering(offering.getId());
            assertNotNull(ret);
            assertEquals(CourseOfferingMapper.toResponse(offering), ret);

        }

        @Test
        void shouldReturnEmptyForNonExistentCourseOffering() {
            assertThrows(ResourceNotFoundException.class, () -> courseOfferingService.getOffering(-1));
        }

        @Test
        void shouldReturnCorrectOfferingsForSemester() {
            var sem2 =  semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2027);

            var course1 = courseSeeder.saveDefault(department);
            var course2 = courseSeeder.saveDefault(department);

            var offering1 = courseOfferingSeeder.saveDefault(
                course1,
                semester,
                "A"
            );

            var offering2 = courseOfferingSeeder.saveDefault(
                course2,
                semester,
                "A"
            );

            var offering3 = courseOfferingSeeder.saveDefault(
                course2,
                sem2,
                "A"
            );

            var ret = courseOfferingService.getOfferingsBySemester(semester.getId());
            assertNotNull(ret);
            assertEquals(2, ret.size());
            var ids = ret.stream().map(CourseOfferingResponse::id).toList();
            assertTrue(ids.contains(offering1.getId()));
            assertTrue(ids.contains(offering2.getId()));

        }

        @Test
        void shouldReturnEmptyListForNonExistentCourseOfferings() {
            var sem = semesterSeeder.saveDefaultSemester(SemesterTerm.SUMMER, 2027);

            var ret = courseOfferingService.getOfferingsBySemester(sem.getId());
            assertNotNull(ret);
            assertEquals(0, ret.size());
        }

    }
}
