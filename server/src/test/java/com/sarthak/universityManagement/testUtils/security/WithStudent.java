package com.sarthak.universityManagement.testUtils.security;

import com.sarthak.universityManagement.common.types.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithMockCustomUser(role = Role.STUDENT)
public @interface WithStudent {
}