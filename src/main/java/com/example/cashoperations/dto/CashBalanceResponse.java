package com.example.cashoperations.dto;

import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class CashBalanceResponse {
    private LocalDateTime timestamp;
    private String cashier;
    private Map<Currency, List<Denomination>> balances;
}
