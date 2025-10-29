package com.example.cashdocumentsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Schema(
        name = "CashOperationsDetails",
        description = "Schema to hold Cash Operations, Cash Reporting and Cash Documents information."
)
public class CashOperationsDetails {
    @Schema(description = "Statistics info in order to calculate total amount of deposits/withdrawals per cashier.")
    private ConcurrentHashMap<String, Integer> operations = new ConcurrentHashMap<>();

    @Schema(description = "Timestamp when the snapshot was generated (UTC)", example = "2025-08-24T20:38:00", type = "string", format = "date-time")
    private LocalDateTime timestamp;

    @Schema(description = "List of Cashier's name for whom the balances are returned", example = "LINDA")
    private List<String> cashiers;

    @Schema(description = "Balances per currency with their denomination breakdown. Keys are currency codes (BGN, EUR).",
            example = "{\n  \"BGN\": [ { \"quantity\": 10, \"value\": 10, \"totalAmount\": 100, \"timestamp\": \"2025-08-24T18:45:00\" }, { \"quantity\": 2, \"value\": 50, \"totalAmount\": 100, \"timestamp\": \"2025-08-24T18:45:00\" } ],\n  \"EUR\": [ { \"quantity\": 1, \"value\": 100, \"totalAmount\": 100, \"timestamp\": \"2025-08-24T18:46:00\" } ]\n}")
    private Map<String, List<Denomination>> balances;



    private String date;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal endOfDayBalance;
    private Map<String, BigDecimal> currencyBreakdown;



    @Schema(
            description = "All files metadata"
    )
    private List<FileDto> filesDtos;
}
