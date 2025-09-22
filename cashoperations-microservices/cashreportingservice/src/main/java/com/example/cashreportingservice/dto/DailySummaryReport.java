package com.example.cashreportingservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class DailySummaryReport {
    private String cashier;
    private String date;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal endOfDayBalance;
    private Map<String, BigDecimal> currencyBreakdown;

    public DailySummaryReport() {
        this.totalDeposits = BigDecimal.ZERO;
        this.totalWithdrawals = BigDecimal.ZERO;
        this.endOfDayBalance = BigDecimal.ZERO;
    }
}
