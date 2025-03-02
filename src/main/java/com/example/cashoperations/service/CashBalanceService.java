package com.example.cashoperations.service;

import com.example.cashoperations.dto.CashBalanceResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CashBalanceService {
    List<CashBalanceResponse> getCashBalances(Optional<LocalDateTime> dateFrom, Optional<LocalDateTime> dateTo, Optional<String> cashier);
}