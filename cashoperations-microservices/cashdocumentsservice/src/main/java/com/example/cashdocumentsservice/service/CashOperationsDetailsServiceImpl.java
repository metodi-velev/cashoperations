package com.example.cashdocumentsservice.service;

import com.example.cashdocumentsservice.dto.*;
import com.example.cashdocumentsservice.model.MyFile;
import com.example.cashdocumentsservice.service.client.CashOperationsFeignClient;
import com.example.cashdocumentsservice.service.client.CashReportingServiceFeignClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@AllArgsConstructor
public class CashOperationsDetailsServiceImpl implements CashOperationsDetailsService {

    private CashOperationsFeignClient cashOperationsFeignClient;
    private CashReportingServiceFeignClient cashReportingServiceFeignClient;
    private CashDocumentsClientService cashDocumentsClientService;

    @Override
    public Mono<CashOperationsDetails> fetchCashOperationsDetails(
            Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo,
            Optional<String> cashier,
            LocalDate date
    ) {

        CashOperationsDetails cashOperationsDetails = new CashOperationsDetails();

        ResponseEntity<List<CashBalanceResponse>> transactions =
                cashOperationsFeignClient.getTransactions(
                        dateFrom,
                        dateTo,
                        cashier
                );

        Optional<List<CashBalanceResponse>> cashBalances = Optional.of(
                Optional.ofNullable(transactions.getBody()).orElseThrow()
        );

        Map<Currency, List<Denomination>> balances = new ConcurrentHashMap<>();
        List<String> cashiers = new ArrayList<>();

        cashBalances.orElseThrow().forEach(cashBalance -> {
            Map<Currency, List<Denomination>> cashBalanceMap = cashBalance.getBalances();
            balances.put(Currency.BGN, cashBalanceMap.get(Currency.BGN));
            balances.put(Currency.EUR, cashBalanceMap.get(Currency.EUR));
            cashiers.add(cashBalance.getCashier());
        });

        cashOperationsDetails.setOperations(cashBalances.orElseThrow().getLast().getOperations());
        cashOperationsDetails.setTimestamp(cashBalances.orElseThrow().getLast().getTimestamp());
        cashOperationsDetails.setCashiers(cashiers);
        cashOperationsDetails.setBalances(balances);

        Mono<ResponseEntity<DailySummaryReport>> dailyReport =
                Mono.fromCallable(() ->
                        cashReportingServiceFeignClient.getDailySummary(
                                date,
                                cashier.orElse(null)
                        )
                );

        Mono<DailySummaryReport> reportPayload = dailyReport
                .flatMap(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return Mono.justOrEmpty(response.getBody());
                    } else {
                        return Mono.error(new RuntimeException("HTTP " + response.getStatusCode()));
                    }
                })
                .doOnNext(report -> log.info("Received report: {}", report))
                .doOnError(error -> log.error("Failed to get report", error));

        reportPayload
                .subscribe(
                        report -> {
                            // Process the report
                            log.info("Report received: {}", report);
                            cashOperationsDetails.setDate(report.getDate());
                            cashOperationsDetails.setTotalDeposits(report.getTotalDeposits());
                            cashOperationsDetails.setTotalWithdrawals(report.getTotalWithdrawals());
                            cashOperationsDetails.setEndOfDayBalance(report.getEndOfDayBalance());
                            cashOperationsDetails.setCurrencyBreakdown(report.getCurrencyBreakdown());
                        },
                        error -> {
                            // Handle error
                            log.error("Error: {}", error.getMessage());
                        },
                        () -> {
                            // Handle completion
                            log.info("Stream completed");
                        }
                );

        List<MyFile> files = cashDocumentsClientService.findAll();
        cashOperationsDetails.setFilesDtos(
                files.stream()
                        .map(cashDocumentsClientService::mapFileToDto)
                        .toList()
        );

        return Mono.just(cashOperationsDetails);
    }
}
