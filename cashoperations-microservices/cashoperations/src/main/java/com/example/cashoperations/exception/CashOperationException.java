package com.example.cashoperations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class CashOperationException extends ResponseStatusException {
    public CashOperationException(HttpStatus status, String reason) {
        super(status, reason);
    }
}

