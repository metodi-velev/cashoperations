package com.example.cashdocumentsservice.service.client;

import com.example.cashdocumentsservice.config.FeignConfig;
import com.example.cashdocumentsservice.dto.CashBalanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@FeignClient(
        name = "cashoperations",
        url = "${cashoperations.service.base-url}",
        configuration = FeignConfig.class
)
public interface CashOperationsFeignClient {

    @GetMapping(value = "/api/v1/cash-balance", consumes = "application/json", produces = "application/json")
    public ResponseEntity<List<CashBalanceResponse>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Optional<LocalDateTime> dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Optional<LocalDateTime> dateTo,
            @RequestParam(required = false) Optional<String> cashier
    );

}
