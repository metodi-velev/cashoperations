package com.example.cashoperations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DenominationNotFoundException extends CashOperationException {
    public DenominationNotFoundException(int value) {
        super(HttpStatus.BAD_REQUEST, "Denomination " + value + " not found in cashier's balance.");
    }
}
