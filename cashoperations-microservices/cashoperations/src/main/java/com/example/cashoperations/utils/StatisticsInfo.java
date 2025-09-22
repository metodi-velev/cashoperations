package com.example.cashoperations.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class StatisticsInfo {
    public static final ConcurrentHashMap<String, Integer> operations = new ConcurrentHashMap<>();
}
