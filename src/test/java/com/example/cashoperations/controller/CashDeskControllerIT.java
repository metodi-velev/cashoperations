package com.example.cashoperations.controller;

import com.example.cashoperations.dto.CashOperationRequest;
import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import com.example.cashoperations.repository.CashierRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CashDeskControllerIT {

    @Value("${fib.auth.api-key}")
    private String apiKey;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CashierRepository cashierRepository;

    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeAll
    void setBeforeAll() {
    }

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    void tearDown() {
        CashierRepository.CASHIERS.clear();
        cashierRepository.init();
    }

    @Test
    void testDepositPerformOperation() throws Exception {
        String jsonRequest = getJsonRequestBodyString("DEPOSIT", "200.00", "LINDA", Arrays.asList(new Denomination(2, 50),
                new Denomination(1, 100)));

        mockMvcPostRequest(jsonRequest);
    }

    @Test
    void testWithdrawalPerformOperation() throws Exception {
        String jsonRequest = getJsonRequestBodyString("WITHDRAWAL", "1000.00", "LINDA", List.of(new Denomination(20, 50)));

        mockMvcPostRequest(jsonRequest);
    }

    @Test
    void testPerformOperation_ShouldThrowInsufficientDenominationException() throws Exception {
        String jsonRequest = getJsonRequestBodyString("WITHDRAWAL", "1050.00", "LINDA", List.of(new Denomination(21, 50)));

        mockMvc.perform(post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("FIB-X-AUTH", apiKey)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest()) // Expect HTTP 400
                .andExpect(jsonPath("$.apiPath").value("uri=/api/v1/cash-operation"))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errorMessage").value("400 BAD_REQUEST \"Insufficient denominations: requested 21x50, but only 20x50 available.\""))
                .andExpect(jsonPath("$.errorTime").isArray()); // Ensure `errorTime` is an array
    }

    private void mockMvcPostRequest(String jsonRequest) throws Exception {
        mockMvc.perform(post("/api/v1/cash-operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("FIB-X-AUTH", apiKey)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Operation successful"));
    }

    private String getJsonRequestBodyString(String operation, String amount, String cashierName, List<Denomination> denominations) throws JsonProcessingException {
        CashOperationRequest request = new CashOperationRequest();
        request.setCashierName(cashierName);
        request.setCurrency(Currency.EUR);
        request.setOperationType(operation);
        request.setAmount(new BigDecimal(amount));
        request.setDenominations(denominations);

        return objectMapper.writeValueAsString(request);
    }
}