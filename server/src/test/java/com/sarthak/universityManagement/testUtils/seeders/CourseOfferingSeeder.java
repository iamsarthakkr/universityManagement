package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingEntity;
import com.sarthak.universityManagement.courseOffering.CourseOfferingRepo;
import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.testUtils.fixtures.CourseOfferingFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.atomic.AtomicInteger;

@TestComponent
@Profile("test")
@Import(InstructorSeeder.class)
public class CourseOfferingSeeder {
    private final CourseOfferingRepo courseOfferingRepo;
    private final AtomicInteger counter =  new AtomicInteger(0);
    private final InstructorSeeder instructorSeeder;

    @Autowired
    public CourseOfferingSeeder(
        CourseOfferingRepo courseOfferingRepo,
        InstructorSeeder instructorSeeder
    ) {
        this.courseOfferingRepo = courseOfferingRepo;
        this.instructorSeeder = instructorSeeder;
    }

    public CourseOfferingEntity save(CourseOfferingEntity courseOfferingEntity) {
        return courseOfferingRepo.saveAndFlush(courseOfferingEntity);
    }

    public CourseOfferingEntity saveDefault(
        CourseEntity course,
        SemesterEntity semester,
        String section
    ) {
        var instructor = instructorSeeder.saveDefaultInstructor(course.getDepartment());
        return courseOfferingRepo.saveAndFlush(
            CourseOfferingFixtures.courseOffering(course, instructor, semester)
                .section(section)
                .build()
        );
    }
}
