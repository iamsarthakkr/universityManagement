package com.sarthak.universityManagement.registration.converter;

import com.sarthak.universityManagement.registration.types.RegistrationAction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StringToRegistrationActionConverter implements Converter<String, RegistrationAction> {
    @Override
    public RegistrationAction convert(String value) {
        return RegistrationAction.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
