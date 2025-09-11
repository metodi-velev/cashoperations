package com.example.cashoperations.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(name = "ErrorResponse", description = "Standard error response returned by the API when an operation fails. The errorMessage varies by status and context, e.g.: 'Invalid API key.' (401), 'DateFrom must be before dateTo' (400), 'Balances map cannot be null.' (400).")
public class ErrorResponseDto {

    @Schema(description = "API path of the request that caused the error", example = "uri=/api/v1/cash-balance")
    private String apiPath;

    @Schema(description = "HTTP status representing the error type", example = "BAD_REQUEST", implementation = String.class)
    private HttpStatus errorCode;

    @Schema(description = "Human-readable error message providing details about the failure. Examples: 'Invalid API key.', 'DateFrom must be before dateTo', 'Balances map cannot be null.'", example = "Invalid API key.")
    private String errorMessage;

    @Schema(description = "Timestamp when the error occurred (UTC)", example = "2025-08-24T20:38:00", type = "string", format = "date-time")
    private LocalDateTime errorTime;
}

