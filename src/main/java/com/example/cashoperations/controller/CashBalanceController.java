package com.example.cashoperations.controller;

import com.example.cashoperations.dto.CashBalanceResponse;
import com.example.cashoperations.service.CashBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@Transactional
@Validated
@RequestMapping("/api/v1")
public class CashBalanceController {
    @Autowired
    private CashBalanceService cashBalanceService;

    @GetMapping("/cash-balance")
    public ResponseEntity<List<CashBalanceResponse>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Optional<LocalDateTime> dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Optional<LocalDateTime> dateTo,
            @RequestParam(required = false) Optional<String> cashier
    ) {
        List<CashBalanceResponse> balances = cashBalanceService.getCashBalances(dateFrom, dateTo, cashier);
        return ResponseEntity.ok(balances);
    }
}
