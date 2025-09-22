package com.example.cashreportingservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CashOperationResponse {
    private Long id;
    private String cashierName;
    private String currency;
    private String operationType; // "DEPOSIT" or "WITHDRAWAL"
    private BigDecimal amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime operationTime;

    // Other fields as needed
}