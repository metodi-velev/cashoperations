package com.example.cashoperations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class LogTransactionException extends CashOperationException {
    public LogTransactionException(String message, String cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message + " Cause: " + cause);
    }
}

