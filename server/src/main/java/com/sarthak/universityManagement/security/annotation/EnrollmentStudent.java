package com.sarthak.universityManagement.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
    "hasRole('ADMIN') || " +
    "@authorizationService.is(#isStudentForEnrollment(#enrollmentId)"
)
public @interface EnrollmentStudent {
}
