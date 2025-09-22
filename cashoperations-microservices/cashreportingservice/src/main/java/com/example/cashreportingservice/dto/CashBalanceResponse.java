package com.example.cashreportingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Schema(name = "CashBalanceResponse", description = "Represents the cash balances snapshot for a cashier at a given timestamp, grouped by currency and broken down by denominations.")
public class CashBalanceResponse {
    @Schema(description = "Statistics info in order to calculate total amount of deposits/withdrawals per cashier.")
    private final ConcurrentHashMap<String, Integer> operations = new ConcurrentHashMap<>();

    @Schema(description = "Timestamp when the snapshot was generated (UTC)", example = "2025-08-24T20:38:00", type = "string", format = "date-time")
    private LocalDateTime timestamp;

    @Schema(description = "Cashier's name for whom the balances are returned", example = "LINDA")
    private String cashier;

    @Schema(description = "Balances per currency with their denomination breakdown. Keys are currency codes (BGN, EUR).",
            example = "{\n  \"BGN\": [ { \"quantity\": 10, \"value\": 10, \"totalAmount\": 100, \"timestamp\": \"2025-08-24T18:45:00\" }, { \"quantity\": 2, \"value\": 50, \"totalAmount\": 100, \"timestamp\": \"2025-08-24T18:45:00\" } ],\n  \"EUR\": [ { \"quantity\": 1, \"value\": 100, \"totalAmount\": 100, \"timestamp\": \"2025-08-24T18:46:00\" } ]\n}")
    private Map<String, List<Denomination>> balances;
}
