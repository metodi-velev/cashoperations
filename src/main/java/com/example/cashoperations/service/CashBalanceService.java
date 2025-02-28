package com.example.cashoperations.service;

import com.example.cashoperations.dto.CashBalanceResponse;
import com.example.cashoperations.exception.InvalidDateRangeException;
import com.example.cashoperations.model.Cashier;
import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import com.example.cashoperations.repository.CashierRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CashBalanceService {

    public List<CashBalanceResponse> getCashBalances(LocalDateTime dateFrom, LocalDateTime dateTo, String cashier) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new InvalidDateRangeException("DateFrom must be before dateTo");
        }

        logFilters(dateFrom, dateTo, cashier);

        Map<String, Cashier> cashiers = CashierRepository.CASHIERS;

        List<CashBalanceResponse> responses = cashiers.entrySet().stream()
                .filter(entry -> (cashier == null || entry.getKey().equalsIgnoreCase(cashier)))
                .map(entry -> {
                    log.debug("Processing balances for cashier: {}", entry.getKey());
                    return new CashBalanceResponse(
                            LocalDateTime.now(),
                            entry.getKey(),
                            filterBalancesByDate(entry.getValue().getBalances(), dateFrom, dateTo)
                    );
                })
                .collect(Collectors.toList());

        if (responses.isEmpty()) {
            log.warn("No cash balances found for cashier: {} in the given date range.", cashier);
        }

        log.debug("Returning {} cash balance responses", responses.size());
        return responses;
    }

    private Map<Currency, List<Denomination>> filterBalancesByDate(
            Map<Currency, List<Denomination>> balances,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {

        if (dateFrom == null && dateTo == null) {
            log.debug("No filtering applied. Returning all balances.");
            return balances;
        }

        log.debug("Filtering balances with date range: from {}, to {}", dateFrom, dateTo);

        Map<Currency, List<Denomination>> filteredBalances = balances.entrySet().stream()
                .map(entry -> {
                    List<Denomination> filteredDenominations = entry.getValue().stream()
                            .filter(denomination -> {
                                LocalDateTime timestamp = denomination.getTimestamp();
                                return (dateFrom == null || !timestamp.isBefore(dateFrom)) &&
                                        (dateTo == null || !timestamp.isAfter(dateTo));
                            })
                            .collect(Collectors.toList());

                    log.debug("Filtered {} denominations for currency: {}", filteredDenominations.size(), entry.getKey());
                    return Map.entry(entry.getKey(), filteredDenominations);
                })
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.debug("Final filtered balances contain {} currencies", filteredBalances.size());
        return filteredBalances;
    }

    private void logFilters(LocalDateTime dateFrom, LocalDateTime dateTo, String cashier) {
        StringBuilder logMessage = new StringBuilder("Fetching cash balances");

        if (cashier != null) logMessage.append(" for cashier: ").append(cashier);
        if (dateFrom != null) logMessage.append(", from: ").append(dateFrom);
        if (dateTo != null) logMessage.append(", to: ").append(dateTo);

        log.info(logMessage.toString());
    }
}
