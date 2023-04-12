package com.api.annotation;

import com.api.annotation.implementation.ExpressionValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@NotNull
@Size(min = 3, max = 16, message = "Expression length must be between 3 and 16")
@Target(ElementType.PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = {ExpressionValidator.class})
@Documented
public @interface ValidExpression {

    String message() default "Expression cannot contain 'all'";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
