package com.example.cashoperations.dto;

import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import com.example.cashoperations.validators.ValidCurrency;
import com.example.cashoperations.validators.ValidDenominations;
import com.example.cashoperations.validators.ValidOperationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Schema(
        name = "CashOperationRequest",
        description = "Request payload to perform a cash DEPOSIT or WITHDRAWAL at the cash desk. " +
                "Includes cashier identification, currency (BGN or EUR), operation type, total amount, and the list of banknote denominations used."
)
public class CashOperationRequest {
    @Schema(description = "Name of the cashier performing the operation",
            example = "LINDA",
            minLength = 2,
            maxLength = 20,
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Cashier's cannot be null and must have a value.")
    @Size(min = 2, max = 20, message = "Cashier's name must have at least 2 and at most 20 letters long.")
    private String cashierName;

    @Schema(description = "Operation currency",
            implementation = Currency.class,
            allowableValues = {"BGN", "EUR"},
            example = "BGN",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Currency cannot be null")
    @ValidCurrency
    private Currency currency;

    @Schema(description = "Type of cash operation",
            allowableValues = {"DEPOSIT", "WITHDRAWAL"},
            example = "DEPOSIT",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @ValidOperationType
    private String operationType; // "DEPOSIT" or "WITHDRAWAL"

    @Schema(description = "Total amount for the operation. Must equal the sum of (denomination.value * denomination.quantity). Minimum 10.00.",
            example = "600.00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount must not be null.")
    @Positive(message = "Amount must be greater than zero.")
    @DecimalMin(value = "10.00", message = "Amount must be at least 10.00.")
    private BigDecimal amount;

    @Schema(description = "List of banknote denominations used for this operation. Allowed values for banknote 'value': 5, 10, 20, 50, 100.",
            example = "[{\"quantity\":10, \"value\":10}, {\"quantity\":10, \"value\":50}]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @ValidDenominations(allowedValues = " 5, 10, 20, 50, 100 ", message = "Denominations only of 5, 10, 20, 50 or 100 BGN/EUR are allowed.")
    private List<Denomination> denominations;

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public List<Denomination> getDenominations() {
        return denominations;
    }

    public void setDenominations(List<Denomination> denominations) {
        this.denominations = denominations;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "CashOperationRequest{" +
                "cashierName='" + cashierName + '\'' +
                ", currency=" + currency +
                ", operationType='" + operationType + '\'' +
                ", amount=" + amount +
                ", denominations=" + denominations.stream().map(Denomination::toString).collect(Collectors.joining(", ")) +
                '}';
    }
}
