package com.api.annotation.implementation;

import com.api.annotation.Number;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public final class NumberValidator implements ConstraintValidator<Number, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            return Integer.parseInt(value) >= 0;
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }
}
