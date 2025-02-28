package com.example.cashoperations.dto;

import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import com.example.cashoperations.validators.ValidCurrency;
import com.example.cashoperations.validators.ValidOperationType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CashOperationRequest {
    @NotBlank(message = "Cashier's cannot be null and must have a value.")
    @Size(min = 2, max = 20, message = "Cashier's name must have at least 2 and at most 20 letters long.")
    private String cashierName;

    @NotNull(message = "Currency cannot be null")
    @ValidCurrency
    private Currency currency;

    @NotBlank
    @ValidOperationType
    private String operationType; // "DEPOSIT" or "WITHDRAWAL"

    @NotNull(message = "Amount must not be null.")
    @Positive(message = "Amount must be greater than zero.")
    @DecimalMin(value = "10.00", message = "Amount must be at least 10.00.")
    private BigDecimal amount;

    @NotNull
    @Size(min = 1, message = "Cash operation must contain at least one denomination.")
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
