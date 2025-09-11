package com.example.cashoperations.controller;

import com.example.cashoperations.dto.CashBalanceResponse;
import com.example.cashoperations.exception.ErrorResponseDto;
import com.example.cashoperations.service.CashBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Tag(
        name = "Cash Balance Queries",
        description = "Endpoint to retrieve cash balances per cashier and currency, filterable by date range and cashier name."
)
@RestController
@Transactional
@Validated
@SecurityRequirement(name = "fibAuth")
@RequestMapping("/api/v1")
public class CashBalanceController {
    @Autowired
    private CashBalanceService cashBalanceService;

    @Operation(
            summary = "Get cash balances",
            description = "Returns the current cash balances per currency and denomination for cashiers. " +
                    "Optional filters: dateFrom/dateTo (format: yyyy-MM-dd'T'HH:mm:ss) to include only denominations whose timestamps fall within the range, and cashier to restrict results to a specific cashier."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balances retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CashBalanceResponse[].class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - validation/business rule error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "InvalidDateRange",
                                            summary = "dateFrom is after dateTo",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-balance\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"DateFrom must be before dateTo\",\n  \"errorTime\": [2025,8,24,20,44,37]\n}"),
                                    @ExampleObject(name = "IllegalArgument",
                                            summary = "Balances map null while filtering",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-balance\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"Balances map cannot be null.\",\n  \"errorTime\": [2025,8,24,20,44,37]\n}")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid API key",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "InvalidApiKey",
                                    summary = "Missing or wrong FIB-X-AUTH header",
                                    value = "{\n  \"apiPath\": \"uri=/api/v1/cash-balance\",\n  \"errorCode\": \"UNAUTHORIZED\",\n  \"errorMessage\": \"Invalid API key.\",\n  \"errorTime\": [2025,8,24,20,44,37]\n}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @GetMapping("/cash-balance")
    public ResponseEntity<List<CashBalanceResponse>> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Optional<LocalDateTime> dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") Optional<LocalDateTime> dateTo,
            @RequestParam(required = false) Optional<String> cashier
    ) {
        List<CashBalanceResponse> balances = cashBalanceService.getCashBalances(dateFrom, dateTo, cashier);
        return ResponseEntity.ok(balances);
    }
}
