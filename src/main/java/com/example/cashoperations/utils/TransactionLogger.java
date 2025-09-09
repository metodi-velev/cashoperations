package com.example.cashoperations.utils;

import com.example.cashoperations.dto.CashOperationRequest;
import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import com.example.cashoperations.repository.CashierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TransactionLogger {

    private final BatchFileWriter batchFileWriter;

    @Async("ioExecutor")
    public void logTransaction(String operation, String cashierName, CashOperationRequest request) {
            String timestamp = LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER);
            String content = String.format("%s - %s: %s %s%n", timestamp, operation, cashierName, request);
            batchFileWriter.enqueueTransaction(content);
    }

    @Async("ioExecutor")
    public void logBalances() {
            String timestamp = LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER);
            String content = CashierRepository.CASHIERS.entrySet()
                    .stream()
                    .map(entry -> {
                        Map<Currency, List<Denomination>> nonZeroBalances = entry.getValue()
                                .getBalances()
                                .entrySet()
                                .stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> e.getValue().stream()
                                                .filter(d -> d.getQuantity() != 0)
                                                .collect(Collectors.toList()),
                                        (a, b) -> a,
                                        () -> new EnumMap<>(Currency.class)
                                ));
                        return timestamp + " - " + entry.getKey() + ": " + nonZeroBalances + "\n";
                    })
                    .collect(Collectors.joining());

            batchFileWriter.enqueueBalance(content);
    }
}
