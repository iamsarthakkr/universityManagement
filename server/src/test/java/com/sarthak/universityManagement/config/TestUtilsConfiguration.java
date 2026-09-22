package com.sarthak.universityManagement.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan("com.sarthak.universityManagement.testUtils")
public class TestUtilsConfiguration {
}