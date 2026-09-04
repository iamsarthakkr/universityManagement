package com.sarthak.universityManagement.testUtils.testConfigs;

import com.sarthak.universityManagement.config.JpaConfig;
import com.sarthak.universityManagement.testUtils.seeders.DepartmentSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.InstructorSeeder;
import com.sarthak.universityManagement.testUtils.seeders.StudentRegistrationSeeder;
import com.sarthak.universityManagement.testUtils.seeders.UserSeeder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import({
        UserSeeder.class,
        DepartmentSeeder.class,
        InstructorSeeder.class,
        StudentRegistrationSeeder.class,
        InstructorRegistrationSeeder.class,
        JpaConfig.class
})
public class RegistrationTestConfig {
}
