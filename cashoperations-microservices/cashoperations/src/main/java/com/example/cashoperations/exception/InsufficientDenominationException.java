package com.example.cashoperations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InsufficientDenominationException extends CashOperationException {
    public InsufficientDenominationException(int requestedQuantity, int requestedValue, int availableQuantity, int availableValue) {
        super(HttpStatus.BAD_REQUEST, "Insufficient denominations: requested " + requestedQuantity + "x" + requestedValue + ", but only "
                + availableQuantity
                + "x"
                + availableValue
                + " available.");
    }
}

