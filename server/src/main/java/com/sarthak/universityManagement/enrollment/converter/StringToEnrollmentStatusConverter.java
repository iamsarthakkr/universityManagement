package com.sarthak.universityManagement.enrollment.converter;

import com.sarthak.universityManagement.enrollment.types.EnrollmentStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StringToEnrollmentStatusConverter implements Converter<String, EnrollmentStatus> {
    @Override
    public EnrollmentStatus convert(String value) {
        return EnrollmentStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
