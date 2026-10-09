package com.sarthak.universityManagement.enrollment.converter;

import com.sarthak.universityManagement.enrollment.types.EnrollmentAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StringToEnrollmentActionConverter implements Converter<String, EnrollmentAction> {
    @Override
    public EnrollmentAction convert(String value) {
        return EnrollmentAction.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
