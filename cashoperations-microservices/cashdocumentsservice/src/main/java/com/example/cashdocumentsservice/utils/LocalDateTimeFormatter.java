package com.example.cashdocumentsservice.utils;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class LocalDateTimeFormatter {
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
}