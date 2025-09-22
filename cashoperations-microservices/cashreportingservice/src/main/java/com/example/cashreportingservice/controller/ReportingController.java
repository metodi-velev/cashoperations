package com.example.cashreportingservice.controller;

import com.example.cashreportingservice.dto.CashBalanceResponse;
import com.example.cashreportingservice.dto.DailySummaryReport;
import com.example.cashreportingservice.service.CashOperationsClientService;
import com.example.cashreportingservice.service.SummaryReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * <p>Test the endpoints:</p>
 * <ul>
 *   <li>
 *     Daily Summary:
 *     <code>GET http://localhost:8081/cashreportingservice/api/v1/reports/daily-summary?date=2025-09-22&cashier=Linda</code>
 *   </li>
 *   <li>
 *     Currency Summary:
 *     <code>GET http://localhost:8081/cashreportingservice/api/v1/reports/currency-summary?date=2025-09-22</code>
 *   </li>
 *   <li>
 *     Cashier Activity:
 *     <code>GET http://localhost:8081/cashreportingservice/api/v1/reports/cashier-activity?startDate=2025-09-21T00:00:00&endDate=2025-09-23T23:59:59&cashier=Linda</code>
 *   </li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

    private final CashOperationsClientService cashOperationsClient;
    private final SummaryReportService summaryReportService;

    public ReportingController(CashOperationsClientService cashOperationsClient, SummaryReportService summaryReportService) {
        this.cashOperationsClient = cashOperationsClient;
        this.summaryReportService = summaryReportService;
    }

    @GetMapping("/daily-summary")
    public Mono<ResponseEntity<DailySummaryReport>> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String cashier) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return cashOperationsClient.fetchCashBalances(startOfDay, endOfDay, cashier)
                .map(balanceList -> summaryReportService.generateDailySummaryReport(balanceList, cashier))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/cashier-activity")
    public Mono<ResponseEntity<List<CashBalanceResponse>>> getCashierActivity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam String cashier) {

        return cashOperationsClient.fetchCashBalances(startDate, endDate, cashier)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/currency-summary")
    public Mono<ResponseEntity<Map<String, BigDecimal>>> getCurrencySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return cashOperationsClient.fetchCashBalances(startOfDay, endOfDay, null)
                .map(summaryReportService::generateCurrencySummary)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
