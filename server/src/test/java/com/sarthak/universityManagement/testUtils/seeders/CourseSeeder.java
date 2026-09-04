package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.course.CourseEntity;
import com.sarthak.universityManagement.course.CourseRepo;
import com.sarthak.universityManagement.instructor.InstructorEntity;
import com.sarthak.universityManagement.testUtils.fixtures.CourseFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
public class CourseSeeder {
    private final CourseRepo courseRepo;

    @Autowired
    public CourseSeeder(CourseRepo courseRepo) {
        this.courseRepo = courseRepo;
    }

    public CourseEntity save(CourseEntity course) {
        return courseRepo.saveAndFlush(course);
    }

    public CourseEntity saveDefault(InstructorEntity instructor) {
        return courseRepo.saveAndFlush(
                CourseFixtures.course(instructor).build()
        );
    }

}
