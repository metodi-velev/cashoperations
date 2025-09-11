package com.example.cashoperations.validators;

import com.example.cashoperations.model.Currency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, Currency> {

    private static final Set<String> ALLOWED_CURRENCIES = Set.of("BGN", "EUR");

    @Override
    public boolean isValid(Currency currency, ConstraintValidatorContext context) {
        return currency != null && ALLOWED_CURRENCIES.contains(currency.name());
    }
}