package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.config.IntegrationTests;
import com.sarthak.universityManagement.course.CourseRepo;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingRepo;
import com.sarthak.universityManagement.department.DepartmentRepo;
import com.sarthak.universityManagement.enrollment.EnrollmentEntity;
import com.sarthak.universityManagement.enrollment.EnrollmentRepo;
import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import com.sarthak.universityManagement.instructor.InstructorRepo;
import com.sarthak.universityManagement.registration.instructor.InstructorRegistrationRepo;
import com.sarthak.universityManagement.registration.student.StudentRegistrationRepo;
import com.sarthak.universityManagement.seed.types.SeedTable;
import com.sarthak.universityManagement.semester.SemesterRepo;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.student.StudentRepo;
import com.sarthak.universityManagement.testUtils.seeders.CourseOfferingSeeder;
import com.sarthak.universityManagement.testUtils.seeders.CourseSeeder;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.EnrollmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.SemesterSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentSeeder;
import com.sarthak.universityManagement.user.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

public class SeedRollbackTests extends IntegrationTests {
    @Autowired
    private SeedApplier seedApplier;
    @Autowired
    private SeedRollback seedRollback;
    @Autowired
    private SeedTracking seedTracking;
    @Autowired
    private DepartmentSeeder departmentSeeder;
    @Autowired
    private CourseSeeder courseSeeder;
    @Autowired
    private SemesterSeeder semesterSeeder;
    @Autowired
    private CourseOfferingSeeder courseOfferingSeeder;
    @Autowired
    private StudentSeeder studentSeeder;
    @Autowired
    private EnrollmentSeeder enrollmentSeeder;
    @Autowired
    private DepartmentRepo departmentRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private InstructorRepo instructorRepo;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private CourseRepo courseRepo;
    @Autowired
    private SemesterRepo semesterRepo;
    @Autowired
    private CourseOfferingRepo courseOfferingRepo;
    @Autowired
    private EnrollmentRepo enrollmentRepo;
    @Autowired
    private StudentRegistrationRepo studentRegistrationRepo;
    @Autowired
    private InstructorRegistrationRepo instructorRegistrationRepo;

    @Test
    void shouldRemoveEverySeededRow() {
        seedApplier.apply(SeedFiles.example(), true);

        var plan = seedRollback.rollback().orElseThrow();

        assertEquals(0, departmentRepo.count());
        assertEquals(0, userRepo.count());
        assertEquals(0, instructorRepo.count());
        assertEquals(0, studentRepo.count());
        assertEquals(0, courseRepo.count());
        assertEquals(0, semesterRepo.count());
        assertEquals(0, courseOfferingRepo.count());
        assertEquals(0, enrollmentRepo.count());
        assertEquals(0, studentRegistrationRepo.count());
        assertEquals(0, instructorRegistrationRepo.count());
        assertTrue(seedTracking.findActiveRun().isEmpty());
        plan.toDelete().keySet().forEach(table -> assertEquals(0, plan.dependentCount(table), table.name()));
    }

    @Test
    void shouldDoNothingWithoutAnActiveRun() {
        var other = departmentSeeder.saveDefault("OTHER");

        assertTrue(seedRollback.dryRun().isEmpty());
        assertTrue(seedRollback.rollback().isEmpty());
        assertTrue(departmentRepo.findById(other.getId()).isPresent());
    }

    @Test
    void shouldKeepDataThatDoesNotReachSeededRows() {
        var independent = independentOffering();
        seedApplier.apply(SeedFiles.example(), true);

        seedRollback.rollback();

        var offering = courseOfferingRepo.findById(independent.getId()).orElseThrow();
        assertEquals("OTHER", offering.getCourse().getDepartment().getCode());
        assertTrue(instructorRepo.findById(offering.getInstructor().getId()).isPresent());
        assertTrue(semesterRepo.findById(offering.getSemester().getId()).isPresent());
    }

    @Test
    void shouldRemoveRowsThatDependOnSeededDataAndFixSeatCounts() {
        var independent = independentOffering();
        seedApplier.apply(SeedFiles.example(), true);

        // A demo visitor, logged in as a seeded student, got enrolled in an offering that isn't seeded
        var seededStudent = studentRepo.findByUserId(userRepo.findByUsername("riya.sharma").orElseThrow().getId()).orElseThrow();
        var demoEnrollment = enrollmentSeeder.save(EnrollmentEntity.builder()
            .student(seededStudent)
            .courseOffering(independent)
            .status(EnrollmentStatus.ENROLLED)
            .build());
        independent.setEnrolled(1);
        courseOfferingSeeder.save(independent);

        // An admin approved someone into a seeded department
        var approvedStudent = studentSeeder.saveDefault(departmentRepo.findByCode("CSE").orElseThrow());

        var dryRun = seedRollback.dryRun().orElseThrow();
        var plan = seedRollback.rollback().orElseThrow();

        assertEquals(dryRun.toDelete(), plan.toDelete());
        assertTrue(enrollmentRepo.findById(demoEnrollment.getId()).isEmpty());
        assertTrue(studentRepo.findById(approvedStudent.getId()).isEmpty());
        assertTrue(userRepo.findById(approvedStudent.getUser().getId()).isEmpty());
        assertEquals(1, plan.dependentCount(SeedTable.STUDENT));

        var survivor = courseOfferingRepo.findById(independent.getId()).orElseThrow();
        assertEquals(0, survivor.getEnrolled());
    }

    @Test
    void shouldReportWithoutDeletingOnDryRun() {
        seedApplier.apply(SeedFiles.example(), true);
        var seeded = enrollmentRepo.count();

        var plan = seedRollback.dryRun().orElseThrow();

        assertEquals(seeded, plan.toDelete().get(SeedTable.ENROLLMENT).size());
        assertEquals(seeded, enrollmentRepo.count());
        assertTrue(seedTracking.findActiveRun().isPresent());
    }

    @Test
    void shouldApplyAgainAfterARollback() {
        var first = seedApplier.apply(SeedFiles.example(), true);
        seedRollback.rollback();

        var second = seedApplier.apply(SeedFiles.example(), true);

        assertTrue(second.applied());
        assertNotEquals(first.runId(), second.runId());
        assertEquals(2, departmentRepo.count());
    }

    private CourseOfferingEntity independentOffering() {
        var department = departmentSeeder.saveDefault("OTHER");
        var course = courseSeeder.saveDefault(department);
        var semester = semesterSeeder.saveDefaultSemester(SemesterTerm.WINTER, 2030);
        return courseOfferingSeeder.saveDefault(course, semester, "A");
    }
}
