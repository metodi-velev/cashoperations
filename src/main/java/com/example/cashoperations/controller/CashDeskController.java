package com.example.cashoperations.controller;

import com.example.cashoperations.dto.CashOperationRequest;
import com.example.cashoperations.exception.ErrorResponseDto;
import com.example.cashoperations.service.CashDeskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Cash Desk Operations",
        description = "Endpoint to perform cash deposit or withdrawal in BGN or EUR."
)
@RestController
@Transactional
@Validated
@SecurityRequirement(name = "fibAuth")
@RequestMapping("/api/v1")
public class CashDeskController {

    private final CashDeskService cashService;

    public CashDeskController(@Qualifier("cashDeskServiceImplV3") CashDeskService cashService) {
        this.cashService = cashService;
    }

    @Operation(
            summary = "Perform cash deposit or withdrawal",
            description = "Processes a cash operation (DEPOSIT or WITHDRAWAL) in BGN or EUR for a given cashier. "
                    + "Validates the amount against the sum of denominations and updates cashier balances accordingly."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Operation completed successfully",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - validation/business rule error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = {
                                    @ExampleObject(name = "InvalidAmount",
                                            summary = "Amount does not match denominations sum",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"Invalid deposit request. Amount 120.00 does not match overall denominations sum 110.00.\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}"),
                                    @ExampleObject(name = "InvalidDepositUndefined",
                                            summary = "Request body is null",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"Invalid deposit request. Deposit request must be defined.\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}"),
                                    @ExampleObject(name = "InvalidDepositNoCashier",
                                            summary = "Cashier cannot be null",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"Invalid deposit request. Cashier cannot be null.\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}"),
                                    @ExampleObject(name = "InvalidDepositNoDenominations",
                                            summary = "At least one denomination required",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"Invalid deposit request. Deposit request must contain at least one valid denomination.\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}"),
                                    @ExampleObject(name = "CurrencyNotSupported",
                                            summary = "Cashier does not support the requested currency",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"Currency EUR is not supported for this cashier.\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}"),
                                    @ExampleObject(name = "InsufficientDenomination",
                                            summary = "Requested quantity exceeds cashier's quantity",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"Insufficient denominations: requested 5x50 but only 2x50 available\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}"),
                                    @ExampleObject(name = "DenominationNotFound",
                                            summary = "Requested banknote value not available",
                                            value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"BAD_REQUEST\",\n  \"errorMessage\": \"Requested denomination 200 not found.\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}")
                            })
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - missing or invalid API key",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "InvalidApiKey",
                                    summary = "Missing or wrong FIB-X-AUTH header",
                                    value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"UNAUTHORIZED\",\n  \"errorMessage\": \"Invalid API key.\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - cashier not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "CashierNotFound",
                                    summary = "Cashier name not present in repository",
                                    value = "{\n  \"apiPath\": \"uri=/api/v1/cash-operation\",\n  \"errorCode\": \"NOT_FOUND\",\n  \"errorMessage\": \"Cashier not found with the given input data name : 'John'\",\n  \"errorTime\": [2025,8,24,21,4,0]\n}"))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PostMapping("/cash-operation")
    public ResponseEntity<String> performOperation(@Valid @RequestBody CashOperationRequest request) {
        cashService.performOperation(request);
        return ResponseEntity.ok("Operation successful");
    }
}