package com.sarthak.universityManagement.testUtils.seeders;

import com.sarthak.universityManagement.semester.SemesterEntity;
import com.sarthak.universityManagement.semester.SemesterRepo;
import com.sarthak.universityManagement.semester.types.SemesterStatus;
import com.sarthak.universityManagement.semester.types.SemesterTerm;
import com.sarthak.universityManagement.testUtils.fixtures.SemesterFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.ActiveProfiles;

@TestComponent
@ActiveProfiles("test")
public final class SemesterSeeder {
    @Autowired
    private SemesterRepo semesterRepo;

    public SemesterEntity saveSemester(SemesterEntity semesterEntity) {
        return semesterRepo.saveAndFlush(semesterEntity);
    }

    public SemesterEntity saveDefaultSemester(SemesterTerm semesterTerm, Integer year) {
        return semesterRepo.saveAndFlush(
            SemesterFixtures.semester().term(semesterTerm).year(year).build()
        );
    }
}
