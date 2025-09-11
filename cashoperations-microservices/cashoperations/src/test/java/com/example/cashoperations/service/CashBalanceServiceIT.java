package com.example.cashoperations.service;

import com.example.cashoperations.dto.CashBalanceResponse;
import com.example.cashoperations.exception.InvalidDateRangeException;
import com.example.cashoperations.model.Cashier;
import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import com.example.cashoperations.repository.CashierRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CashBalanceServiceIT {

    @Autowired
    private CashBalanceService cashBalanceService;

    private Map<Currency, List<Denomination>> sampleBalances;

    @BeforeAll
    void setup() {
        sampleBalances = extractBalancesFromCashiers(CashierRepository.CASHIERS.values().stream());
    }

    @Test
    void shouldReturnAllBalancesWhenNoFiltersApplied() {
        List<CashBalanceResponse> cashBalanceResponseList = cashBalanceService.getCashBalances(
                Optional.empty(), Optional.empty(), Optional.empty()
        );

        Map<Currency, List<Denomination>> resultMap = extractBalancesFromCashiers(
                cashBalanceResponseList
                        .stream()
                        .map(cb -> new Cashier(cb.getCashier(), cb.getBalances()))
        );

        assertEquals(sampleBalances, resultMap);
    }

    @Test
    void shouldFilterBalancesByDateRangeAndCashierName() {
        Params params = initParams(LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(1), "Linda");

        List<CashBalanceResponse> cashBalanceResponseList = cashBalanceService.getCashBalances(
                params.dateFrom(), params.dateTo(), params.cashier()
        );

        assertEquals(1, cashBalanceResponseList.size());
        assertEquals(4, sumDenominations(cashBalanceResponseList.stream())); // All EUR & BGN denominations should be included
    }

    @Test
    void shouldReturnEmptyWhenNoMatchesFound() {
        Params params = initParams(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), null);

        List<CashBalanceResponse> cashBalanceResponseList = cashBalanceService.getCashBalances(
                params.dateFrom, params.dateTo, params.cashier
        );

        assertTrue(cashBalanceResponseList.stream()
                .flatMap(cbr -> cbr.getBalances().values().stream())
                .allMatch(List::isEmpty));
    }

    @Test
    void shouldThrowExceptionWhenDateFromIsAfterDateTo() {
        Params params = initParams(LocalDateTime.now(), LocalDateTime.now().minusDays(1), null);

        assertThrows(InvalidDateRangeException.class, () ->
                cashBalanceService.getCashBalances(
                        params.dateFrom, params.dateTo, params.cashier
                ));
    }

    private Map<Currency, List<Denomination>> extractBalancesFromCashiers(Stream<Cashier> cashiers) {
        return cashiers
                .map(Cashier::getBalances)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldV, newV) -> Stream.concat(oldV.stream(), newV.stream()).toList()
                ));
    }

    private int sumDenominations(Stream<CashBalanceResponse> cashBalanceResponseStream) {
        return cashBalanceResponseStream
                .flatMap(cbr -> cbr.getBalances().entrySet().stream()) // Flatten balances
                .mapToInt(entry -> entry.getValue().size()) // Count denominations
                .sum(); // Sum total entries
    }

    private Params initParams(LocalDateTime from, LocalDateTime to, String cashierName) {
        return new Params(Optional.ofNullable(from), Optional.ofNullable(to), Optional.ofNullable(cashierName));
    }

    private record Params(Optional<LocalDateTime> dateFrom, Optional<LocalDateTime> dateTo, Optional<String> cashier) {
    }
}