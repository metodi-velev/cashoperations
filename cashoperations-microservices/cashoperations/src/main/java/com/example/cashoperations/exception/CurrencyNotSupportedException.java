package com.example.cashoperations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CurrencyNotSupportedException extends CashOperationException {
    public CurrencyNotSupportedException(String currency) {
        super(HttpStatus.BAD_REQUEST, "Currency " + currency + " is not supported for this cashier.");
    }
}

