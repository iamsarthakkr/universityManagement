package com.sarthak.universityManagement.semester.converter;

import com.sarthak.universityManagement.semester.types.SemesterAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StringToSemesterActionConverter implements Converter<String, SemesterAction> {
    @Override
    public SemesterAction convert(String value) {
        return SemesterAction.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
