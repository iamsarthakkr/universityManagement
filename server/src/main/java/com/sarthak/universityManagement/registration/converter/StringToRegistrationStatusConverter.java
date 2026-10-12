package com.sarthak.universityManagement.registration.converter;

import com.sarthak.universityManagement.registration.types.RegistrationStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StringToRegistrationStatusConverter implements Converter<String, RegistrationStatus> {
    @Override
    public RegistrationStatus convert(String value) {
        return RegistrationStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
