package com.example.cashreportingservice.service;

import com.example.cashreportingservice.dto.CashBalanceResponse;
import com.example.cashreportingservice.dto.DailySummaryReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SummaryReportService {
    public DailySummaryReport generateDailySummaryReport(List<CashBalanceResponse> balanceList, String cashier) {
        DailySummaryReport report = new DailySummaryReport();

        if (balanceList.isEmpty()) {
            return report;
        }

        CashBalanceResponse firstBalance = balanceList.get(0);
        CashBalanceResponse lastBalance = balanceList.get(balanceList.size() - 1);
        if(Objects.nonNull(cashier)) {
            report.setCashier(cashier);
        } else {
            report.setCashier("ALL");
        }
        report.setDate(firstBalance.getTimestamp().toLocalDate().toString());

        // Calculate totals from all balances
        Map<String, BigDecimal> currencyTotals = new ConcurrentHashMap<>();

        balanceList.forEach(balance -> {
            balance.getBalances().forEach((currency, denominations) -> {
                BigDecimal currencyTotal = denominations.stream()
                        .map(denom -> BigDecimal.valueOf(denom.getTotalAmount()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                currencyTotals.merge(currency, currencyTotal, BigDecimal::add);
            });
        });

        report.setCurrencyBreakdown(currencyTotals);
        int totalDeposits = 0;
        int totalWithdrawals = 0;
        if(Objects.nonNull(firstBalance)) {
            totalDeposits = getOperationsSum(firstBalance, "DEPOSIT", Optional.ofNullable(cashier));
            totalWithdrawals = getOperationsSum(firstBalance, "WITHDRAWAL", Optional.ofNullable(cashier));
        }
        report.setTotalDeposits(BigDecimal.valueOf(totalDeposits));
        report.setTotalWithdrawals(BigDecimal.valueOf(totalWithdrawals));

        // For simplicity, assuming the last balance represents end of day
        BigDecimal endOfDayTotal = currencyTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.setEndOfDayBalance(endOfDayTotal);

        return report;
    }

    private int getOperationsSum(CashBalanceResponse balance, String operation, Optional<String> cashier) {
        log.info("The operations map has value: {}", balance.getOperations());
        return balance.getOperations().entrySet().stream()
                .filter(entry -> entry.getKey().toUpperCase().contains(operation))
                .filter(entry -> cashier.map(cashierName -> entry.getKey().toUpperCase().contains(cashierName.toUpperCase())).orElse(true))
                .mapToInt(Map.Entry::getValue)
                .sum();
    }

    public Map<String, BigDecimal> generateCurrencySummary(List<CashBalanceResponse> balanceList) {
        Map<String, BigDecimal> currencySummary = new ConcurrentHashMap<>();

        balanceList.forEach(balance -> {
            balance.getBalances().forEach((currency, denominations) -> {
                BigDecimal currencyTotal = denominations.stream()
                        .map(denom -> BigDecimal.valueOf(denom.getTotalAmount()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                currencySummary.merge(currency, currencyTotal, BigDecimal::add);
            });
        });

        return currencySummary;
    }
}
