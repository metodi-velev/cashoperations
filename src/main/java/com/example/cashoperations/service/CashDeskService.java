package com.example.cashoperations.service;

import com.example.cashoperations.dto.CashOperationRequest;
import com.example.cashoperations.exception.*;
import com.example.cashoperations.model.Cashier;
import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import com.example.cashoperations.repository.CashierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CashDeskService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private final CashierRepository cashierRepository;

    public void performOperation(CashOperationRequest request) {
        Cashier cashier = cashierRepository.getCashier(request.getCashierName());
        if (cashier == null) {
            throw new ResourceNotFoundException("Cashier", "name", request.getCashierName());
        }

        chechAmountValidity(request);

        if ("DEPOSIT".equals(request.getOperationType())) {
            deposit(cashier, request);
        } else if ("WITHDRAWAL".equals(request.getOperationType())) {
            withdraw(cashier, request);
        }

        cashierRepository.updateCashier(cashier);
    }

    private void deposit(Cashier cashier, CashOperationRequest request) {
        if (request == null) {
            log.error("Invalid deposit request. Cashier or denominations cannot be null/empty.");
            throw new InvalidDepositException("Invalid deposit request. Deposit request must be defined.");
        }
        if (cashier == null) {
            log.error("Invalid deposit request. Cashier cannot be null.");
            throw new InvalidDepositException("Invalid deposit request. Cashier cannot be null.");
        }
        if (request.getDenominations() == null || request.getDenominations().isEmpty()) {
            log.error("Invalid deposit request. Cashier or denominations cannot be null/empty.");
            throw new InvalidDepositException("Invalid deposit request. Deposit request must contain at least one valid denomination.");
        }

        Currency currency = request.getCurrency();
        List<Denomination> depositDenominations = request.getDenominations();
        Map<Currency, List<Denomination>> cashierBalances = cashier.getBalances();

        // Retrieve or initialize the list of denominations for the given currency
        List<Denomination> cashierDenominations =
                cashierBalances.computeIfAbsent(currency, k -> new ArrayList<>());

        // Update cashier balance for the specified currency
        for (Denomination deposit : depositDenominations) {
            Optional<Denomination> existingDenomination = cashierDenominations.stream()
                    .filter(d -> d.getValue() == deposit.getValue())
                    .findFirst();
            synchronized (this) {
                if (existingDenomination.isPresent()) {
                    // Update the quantity of the existing denomination
                    existingDenomination.get().setQuantity(
                            existingDenomination.get().getQuantity() + deposit.getQuantity()
                    );
                } else {
                    // Add new denomination entry
                    cashierDenominations.add(new Denomination(deposit.getQuantity(), deposit.getValue()));
                }
            }
        }

        log.info("Deposit successful: {} {} deposit from cashier {}", request.getAmount(), request.getCurrency(), cashier.getName());
        logTransaction("DEPOSIT", cashier.getName(), request);
        logBalances();
    }


    private synchronized void withdraw(Cashier cashier, CashOperationRequest request) {
        log.info("Processing withdrawal of {} {} for cashier {}", request.getAmount(), request.getCurrency(), cashier.getName());

        // Get the cashier's balance for the requested currency
        Map<Currency, List<Denomination>> balances = cashier.getBalances();
        List<Denomination> cashierDenominations = balances.get(request.getCurrency());

        if (cashierDenominations == null) {
            log.error("Currency {} not supported for cashier {}", request.getCurrency(), cashier.getName());
            throw new CurrencyNotSupportedException(request.getCurrency().toString());
        }

        // Create a copy of the cashier's denominations to avoid modifying the original list directly
        List<Denomination> updatedDenominations = new ArrayList<>();

        for (Denomination cashierDenomination : cashierDenominations) {
            updatedDenominations.add(new Denomination(cashierDenomination.getQuantity(), cashierDenomination.getValue()));
        }

        // Process the requested denominations
        for (Denomination requestedDenomination : request.getDenominations()) {
            boolean found = false;
            for (Denomination cashierDenomination : updatedDenominations) {
                if (cashierDenomination.getValue() == requestedDenomination.getValue()) {
                    // Check if the cashier has enough of this denomination
                    if (cashierDenomination.getQuantity() < requestedDenomination.getQuantity()) {
                        log.error("Insufficient denominations: requested {}x{} but only {}x{} available",
                                requestedDenomination.getQuantity(), requestedDenomination.getValue(), cashierDenomination.getQuantity(),
                                cashierDenomination.getValue());
                        throw new InsufficientDenominationException(
                                requestedDenomination.getQuantity(),
                                requestedDenomination.getValue(),
                                cashierDenomination.getQuantity(),
                                cashierDenomination.getValue()
                        );
                    }
                    synchronized (this) {
                        // Subtract the requested quantity
                        cashierDenomination.setQuantity(cashierDenomination.getQuantity() - requestedDenomination.getQuantity());
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                log.error("Denomination {} not available for cashier {}", requestedDenomination.getValue(), cashier.getName());
                throw new DenominationNotFoundException(requestedDenomination.getValue());
            }
        }

        // Update the cashier's balance
        balances.put(request.getCurrency(), updatedDenominations);
        cashier.setBalances(balances);

        log.info("Withdrawal successful: {} {} withdrawn from cashier {}", request.getAmount(), request.getCurrency(), cashier.getName());
        logTransaction("Withdraw", cashier.getName(), request);
        logBalances();
    }

    private void chechAmountValidity(CashOperationRequest request) {
        BigDecimal amount = request.getAmount();

        int denominationsAmountSum = request.getDenominations().stream()
                .map(d -> d.getValue() * d.getQuantity())
                .mapToInt(d -> d)
                .sum();

        BigDecimal bigDecimalDenominationsAmountSum = new BigDecimal(denominationsAmountSum);

        if (amount.compareTo(bigDecimalDenominationsAmountSum) != 0) {
            log.error("Invalid deposit request. Amount {} does not match overall denominations sum {}.", amount, bigDecimalDenominationsAmountSum);
            throw new InvalidAmountException("Invalid deposit request. Amount "
                    + amount
                    + " does not match overall denominations sum "
                    + bigDecimalDenominationsAmountSum + "."
            );
        }
    }

    private void logTransaction(String operation, String cashierName, CashOperationRequest request) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String logEntry = String.format("%s - %s: %s %s%n", timestamp, operation, cashierName, request);
        try {
            Files.write(Paths.get(CashierRepository.TRANSACTION_FILE), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to log transaction", e);
        }
    }

    private void logBalances() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        StringBuilder sb = new StringBuilder();
        CashierRepository.CASHIERS.forEach((name, cashier) -> sb.append(timestamp).append(" - ").append(name).append(": ").append(cashier.getBalances()).append("\n"));
        try {
            Files.write(Paths.get(CashierRepository.BALANCE_FILE), sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to log balances", e);
        }
    }
}
