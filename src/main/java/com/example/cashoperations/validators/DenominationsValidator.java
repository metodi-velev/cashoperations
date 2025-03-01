package com.example.cashoperations.validators;

import com.example.cashoperations.model.Denomination;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class DenominationsValidator implements ConstraintValidator<ValidDenominations, List<Denomination>> {

    private static final Set<Integer> ALLOWED_DENOMINATIONS = Set.of(5, 10, 20, 50, 100);

    @Override
    public boolean isValid(List<Denomination> denominations, ConstraintValidatorContext context) {
        // Disable default violation messages
        context.disableDefaultConstraintViolation();

        // Null check
        if (denominations == null) {
            return buildDenominationViolation(context, "Denominations cannot be null.");
        }

        // Empty check
        if (denominations.isEmpty()) {
            return buildDenominationViolation(context, "Denominations cannot be empty.");
        }

        // Find invalid denominations
        Set<Integer> invalidDenominations = denominations.stream()
                .map(Denomination::getValue)
                .filter(value -> !ALLOWED_DENOMINATIONS.contains(value))
                .collect(Collectors.toCollection(TreeSet::new));

        // If there are invalid values, create a formatted message
        if (!invalidDenominations.isEmpty()) {
            String errorMessage = formatInvalidDenominationsMessage(invalidDenominations);
            return buildDenominationViolation(context, errorMessage);
        }

        return true;
    }

    /**
     * Helper method to add a constraint violation with the given message.
     */
    private boolean buildDenominationViolation(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

    /**
     * Helper method to generate an error message for invalid denominations.
     */
    private String formatInvalidDenominationsMessage(Set<Integer> invalidDenominations) {
        String invalidDenominationsString = String.join(", ", invalidDenominations.stream()
                .map(String::valueOf)
                .toList());

        String validDenominationsString = ALLOWED_DENOMINATIONS.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));

        return String.format(
                "Invalid denominations: %s. Denominations only of %s BGN/EUR are allowed.",
                invalidDenominationsString, validDenominationsString
        );
    }
}