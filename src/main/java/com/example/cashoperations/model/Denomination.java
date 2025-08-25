package com.example.cashoperations.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "Denomination", description = "Represents a banknote denomination with its quantity and derived total amount. " +
        "Allowed banknote values: 5, 10, 20, 50, 100. Timestamp marks when this denomination entry was recorded/updated (UTC).")
public class Denomination {
    @Schema(description = "Number of banknotes of the given denomination value", example = "10", minimum = "0")
    private int quantity;

    @Schema(description = "Banknote face value", example = "50", allowableValues = {"5","10","20","50","100"})
    private int value;

    @Schema(description = "Derived total amount = quantity * value", example = "500", readOnly = true)
    private int totalAmount;

    @Schema(description = "Timestamp when the denomination entry was recorded/updated (UTC)", example = "2025-08-24T18:45:00", type = "string", format = "date-time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime timestamp;

    public Denomination() {
    }

    public Denomination(int quantity, int value) {
        this.quantity = quantity;
        this.value = value;
        this.totalAmount += quantity * value;
        timestamp = LocalDateTime.now();
    }

    public Denomination(int quantity, int value, LocalDateTime timestamp) {
        this(quantity, value);
        this.timestamp = timestamp;
    }

    public Denomination(int quantity, int value, int totalAmount) {
        this(quantity, value);
        //this.totalAmount = totalAmount;
        this.timestamp = LocalDateTime.now();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalAmount = quantity * value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return quantity + "x" + value;
    }
}
