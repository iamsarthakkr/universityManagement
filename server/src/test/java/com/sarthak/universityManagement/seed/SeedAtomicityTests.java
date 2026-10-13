package com.sarthak.universityManagement.seed;

import com.sarthak.universityManagement.config.MySqlTestContainer;
import com.sarthak.universityManagement.config.TestClockConfig;
import com.sarthak.universityManagement.config.TestUtilsConfiguration;
import com.sarthak.universityManagement.department.DepartmentRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

// Not @Transactional: a failed apply must roll back its own transaction, which a test transaction would hide
@SpringBootTest
@ActiveProfiles("test")
@Import({
    TestUtilsConfiguration.class,
    TestClockConfig.class,
})
public class SeedAtomicityTests extends MySqlTestContainer {
    private static final String DEPARTMENT_CODE = "ATOMIC";

    @Autowired
    private SeedApplier seedApplier;
    @Autowired
    private SeedTracking seedTracking;
    @Autowired
    private DepartmentRepo departmentRepo;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        // Only does anything if the test failed and left rows behind
        jdbcTemplate.update("DELETE FROM course WHERE departmentId IN (SELECT id FROM department WHERE code = ?)", DEPARTMENT_CODE);
        jdbcTemplate.update("DELETE FROM department WHERE code = ?", DEPARTMENT_CODE);
    }

    @Test
    void shouldLeaveNothingBehindWhenAReferenceFailsHalfwayThrough() {
        var json = """
            { "formatVersion": 2,
              "departments": [ { "code": "%s", "name": "Saved before the failure" } ],
              "courses": [ { "code": "C1", "department": "MISSING", "title": "t", "description": "d", "credits": 3 } ] }
            """.formatted(DEPARTMENT_CODE);

        assertThrows(SeedException.class, () -> seedApplier.apply(SeedFiles.json(json), true));

        assertTrue(departmentRepo.findByCode(DEPARTMENT_CODE).isEmpty());
        assertTrue(seedTracking.findActiveRun().isEmpty());
    }
}
