package com.example.cashoperations.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class OperationTypeValidator implements ConstraintValidator<ValidOperationType, String> {

    private static final Set<String> ALLOWED_OPERATION_TYPES = Set.of("DEPOSIT", "WITHDRAWAL");

    @Override
    public boolean isValid(String operationType, ConstraintValidatorContext context) {
        return operationType != null && ALLOWED_OPERATION_TYPES.contains(operationType.toUpperCase());
    }
}