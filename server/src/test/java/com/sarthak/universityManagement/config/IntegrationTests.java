package com.sarthak.universityManagement.config;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import({
    TestUtilsConfiguration.class,
    TestClockConfig.class,
})
@Transactional
public abstract class IntegrationTests extends MySqlTestContainer {
    @Autowired
    private MutableTestClock testClock;

    @AfterEach
    void resetClock() {
        testClock.reset();
    }
}
