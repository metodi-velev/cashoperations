package com.example.cashdocumentsservice.service;

import com.example.cashdocumentsservice.dto.CashOperationsDetails;
import org.springframework.format.annotation.DateTimeFormat;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CashOperationsDetailsService {
    /**
     * @param dateFrom
     * @param dateTo
     * @param cashier
     * @param date
     * @return
     */
    Mono<CashOperationsDetails> fetchCashOperationsDetails(
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Optional<LocalDateTime> dateFrom,
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Optional<LocalDateTime> dateTo,
            Optional<String> cashier,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}
