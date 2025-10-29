package com.example.cashreportingservice.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Currency {
    BGN,
    EUR,
    @JsonEnumDefaultValue
    UNKNOWN
}
