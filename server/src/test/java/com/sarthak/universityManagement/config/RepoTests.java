package com.sarthak.universityManagement.config;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    JpaConfig.class,
    TestUtilsConfiguration.class,
    TestClockConfig.class,
})
@Transactional
public abstract class RepoTests extends MySqlTestContainer {
    @Autowired
    private MutableTestClock testClock;

    @AfterEach
    void resetClock() {
        testClock.reset();
    }
}
