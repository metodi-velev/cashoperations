package com.example.cashoperations.validators;

import com.example.cashoperations.model.Denomination;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

public class DenominationsValidator implements ConstraintValidator<ValidDenominations, List<Denomination>> {

    private static final List<Integer> ALLOWED_DENOMINATIONS = new ArrayList<>(List.of(5, 10, 20, 50, 100));

    @Override
    public boolean isValid(List<Denomination> denominations, ConstraintValidatorContext context) {
        return denominations.stream()
                .allMatch(denomination ->
                        ALLOWED_DENOMINATIONS.contains(denomination.getValue()));
    }
}