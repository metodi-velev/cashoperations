package com.example.cashoperations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class LogBalancesException extends CashOperationException {
    public LogBalancesException(String message, String cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message + " Cause: " + cause);
    }
}

