package com.example.cashoperations.service;

import com.example.cashoperations.dto.CashOperationRequest;

public interface CashDeskService {
    void performOperation(CashOperationRequest request);
}
