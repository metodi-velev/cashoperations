package com.example.cashreportingservice.service;

import com.example.cashreportingservice.dto.CashBalanceResponse;
import com.example.cashreportingservice.dto.CashOperationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CashOperationsClientService {

    private final WebClient webClient;
    private final String cashOperationsBaseUrl;
    private final String cashOperationsApiKey;

    public CashOperationsClientService(WebClient.Builder webClientBuilder,
                                       @Value("${cashoperations.service.base-url}") String baseUrl,
                                       @Value("${cashoperations.service.api-key}") String apiKey) {
        this.cashOperationsBaseUrl = baseUrl;
        this.cashOperationsApiKey = apiKey;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("FIB-X-AUTH", apiKey)
                .build();
    }

    public Mono<List<CashBalanceResponse>> fetchCashBalances(LocalDateTime dateFrom,
                                                             LocalDateTime dateTo,
                                                             String cashier) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/api/v1/cash-balance")
                .queryParamIfPresent("dateFrom", Optional.ofNullable(dateFrom))
                .queryParamIfPresent("dateTo", Optional.ofNullable(dateTo))
                .queryParamIfPresent("cashier", Optional.ofNullable(cashier));

        return webClient.get()
                .uri(uriBuilder.build().toUriString())
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "Error from CashOperations service: " + errorBody))))
                .bodyToFlux(CashBalanceResponse.class)
                .collectList();
    }

    public Mono<CashBalanceResponse> fetchLatestCashBalance(String cashier) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();

        return fetchCashBalances(startOfDay, now, cashier)
                .flatMap(balances -> {
                    if (balances.isEmpty()) {
                        return Mono.empty();
                    }
                    return Mono.just(balances.get(balances.size() - 1));
                });
    }

    public Mono<List<CashOperationResponse>> fetchCashOperations(LocalDateTime dateFrom,
                                                                 LocalDateTime dateTo,
                                                                 String cashier,
                                                                 String operationType) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/api/v1/cash-operations") // This endpoint would need to exist
                .queryParamIfPresent("dateFrom", Optional.ofNullable(dateFrom))
                .queryParamIfPresent("dateTo", Optional.ofNullable(dateTo))
                .queryParamIfPresent("cashier", Optional.ofNullable(cashier))
                .queryParamIfPresent("operationType", Optional.ofNullable(operationType));

        return webClient.get()
                .uri(uriBuilder.build().toUriString())
                .header("FIB-X-AUTH", cashOperationsApiKey)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException(
                                        "Error from CashOperations service: " + errorBody))))
                .bodyToFlux(CashOperationResponse.class)
                .collectList();
    }
}
