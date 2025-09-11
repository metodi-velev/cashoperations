package com.example.cashoperations.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "cashoperations")
public record CashoperationsContactInfoDto(String message, Map<String, String> contactDetails, List<String> onCallSupport) {

}
