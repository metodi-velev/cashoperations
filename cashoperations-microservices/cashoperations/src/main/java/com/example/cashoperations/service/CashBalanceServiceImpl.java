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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CashBalanceServiceImpl implements CashBalanceService {

    @Override
    public List<CashBalanceResponse> getCashBalances(Optional<LocalDateTime> dateFrom, Optional<LocalDateTime> dateTo, Optional<String> cashier) {
        if (dateFrom.isPresent() && dateTo.isPresent() && dateFrom.get().isAfter(dateTo.get())) {
            throw new InvalidDateRangeException("DateFrom must be before dateTo");
        }

        logFilters(dateFrom, dateTo, cashier);

        Map<String, Cashier> cashiers = CashierRepository.CASHIERS;

        List<CashBalanceResponse> cashBalanceResponses = cashiers.entrySet().stream()
                .filter(entry -> cashier.map(cName -> entry.getKey().equalsIgnoreCase(cName)).orElse(true))
                .map(entry -> {
                    log.debug("Processing balances for cashier: {}", entry.getKey());
                    return new CashBalanceResponse(
                            LocalDateTime.now(),
                            entry.getKey(),
                            filterBalancesByDate(entry.getValue().getBalances(), dateFrom, dateTo)
                    );
                })
                .toList();

        if (cashBalanceResponses.isEmpty()) {
            log.warn("No cash balances found for cashier: {} in the given date range.", cashier);
        }

        log.debug("Returning {} cash balance responses", cashBalanceResponses.size());
        return cashBalanceResponses;
    }

    private Map<Currency, List<Denomination>> filterBalancesByDate(
            Map<Currency, List<Denomination>> balances,
            Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo) {

        if (dateFrom.isEmpty() && dateTo.isEmpty()) {
            log.debug("No filtering applied. Returning all balances.");
            return balances;
        }

        if (Objects.isNull(balances)) {
            throw new IllegalArgumentException("Balances map cannot be null.");
        }

        log.debug("Filtering balances with date range: from {}, to {}", dateFrom.orElse(null), dateTo.orElse(null));

        Map<Currency, List<Denomination>> filteredBalances = balances.entrySet().stream()
                .map(entry -> {
                    List<Denomination> filteredDenominations = entry.getValue().stream()
                            .filter(denomination -> isWithinDateRange(denomination.getTimestamp(), dateFrom, dateTo))
                            .toList();

                    log.debug("Filtered {} denominations for currency: {}", filteredDenominations.size(), entry.getKey());
                    return filteredDenominations.isEmpty() ? null : Map.entry(entry.getKey(), filteredDenominations);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        log.debug("Final filtered balances contain {} currencies", filteredBalances.size());
        return filteredBalances;
    }

    private boolean isWithinDateRange(LocalDateTime timestamp,
                                      Optional<LocalDateTime> dateFrom,
                                      Optional<LocalDateTime> dateTo) {
        return dateFrom.map(from -> !timestamp.isBefore(from)).orElse(true) &&
                dateTo.map(to -> !timestamp.isAfter(to)).orElse(true);
    }

    private void logFilters(Optional<LocalDateTime> dateFrom, Optional<LocalDateTime> dateTo, Optional<String> cashier) {
        StringBuilder logMessage = new StringBuilder("Fetching cash balances");

        if (cashier.isPresent()) logMessage.append(" for cashier: ").append(cashier);
        if (dateFrom.isPresent()) logMessage.append(", from: ").append(dateFrom);
        if (dateTo.isPresent()) logMessage.append(", to: ").append(dateTo);

        log.info(logMessage.toString());
    }
}
