package com.example.cashoperations.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DenominationsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDenominations {

    String message() default "Invalid denomination. Allowed denominations: 5, 10, 20, 50 or 100 BGN/EUR.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String allowedValues() default " 5, 10, 20, 50, 100";
}
