package com.example.cashdocumentsservice.service.client;

import com.example.cashdocumentsservice.config.FeignConfig;
import com.example.cashdocumentsservice.dto.DailySummaryReport;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(
        name = "cashreportingservice",
        url = "${cashreportingservice.service.base-url}",
        configuration = FeignConfig.class
)
public interface CashReportingServiceFeignClient {

    @GetMapping(value = "/api/v1/reports/daily-summary", consumes = "application/json", produces = "application/json")
    public ResponseEntity<DailySummaryReport> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String cashier);

}
