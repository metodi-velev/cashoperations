package com.example.cashoperations.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class Denomination {
    private int quantity;
    private int value;
    private int totalAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime timestamp;

    public Denomination() {
    }

    public Denomination(int quantity, int value) {
        this.quantity = quantity;
        this.value = value;
        timestamp = LocalDateTime.now();
    }

    public Denomination(int quantity, int value, LocalDateTime timestamp) {
        this(quantity, value);
        this.timestamp = timestamp;
    }

    public Denomination(int quantity, int value, int totalAmount) {
        this(quantity, value);
        this.totalAmount = totalAmount;
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
