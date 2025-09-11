package com.example.cashoperations.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Currency {
    BGN,
    EUR,
    @JsonEnumDefaultValue
    UNKNOWN
}
