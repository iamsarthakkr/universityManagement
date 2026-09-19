package com.sarthak.universityManagement.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestUtilsConfiguration.class)
public abstract class IntegrationTests {
}
