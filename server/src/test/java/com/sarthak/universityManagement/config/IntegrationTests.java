package com.sarthak.universityManagement.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestUtilsConfiguration.class, TestClockConfig.class})
@Transactional
public abstract class IntegrationTests {
}
