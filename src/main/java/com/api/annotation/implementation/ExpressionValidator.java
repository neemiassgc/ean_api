package com.api.annotation.implementation;

import com.api.annotation.ValidExpression;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public final class ExpressionValidator implements ConstraintValidator<ValidExpression, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !value.equals("all");
    }
}