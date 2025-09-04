package com.example.cashoperations.service;

import com.example.cashoperations.dto.CashOperationRequest;
import com.example.cashoperations.exception.*;
import com.example.cashoperations.model.Cashier;
import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import com.example.cashoperations.repository.CashierRepository;
import com.example.cashoperations.utils.LocalDateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service("cashDeskServiceImpl")
public class CashDeskServiceImpl implements CashDeskService {

    @Autowired
    private final CashierRepository cashierRepository;

    // Fine-grained locks per cashier+currency to reduce contention versus synchronizing the whole service instance
    private final ConcurrentHashMap<String, ReentrantLock> balanceLocks = new ConcurrentHashMap<>();

    private ReentrantLock getBalanceLock(String cashierName, Currency currency) {
        String key = cashierName + "|" + currency.name();
        return balanceLocks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    @Override
    public void performOperation(CashOperationRequest request) {
        Cashier cashier = cashierRepository.getCashier(request.getCashierName());
        if (cashier == null) {
            throw new ResourceNotFoundException("Cashier", "name", request.getCashierName());
        }

        chechAmountValidity(request);

        if ("DEPOSIT".equalsIgnoreCase(request.getOperationType())) {
            deposit(cashier, request);
        } else if ("WITHDRAWAL".equalsIgnoreCase(request.getOperationType())) {
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

        // Use fine-grained lock per cashier+currency to avoid global contention
        ReentrantLock lock = getBalanceLock(cashier.getName(), currency);
        lock.lock();
        try {
            // Retrieve or initialize the list of denominations for the given currency
            List<Denomination> cashierDenominations =
                    cashierBalances.computeIfAbsent(currency, k -> new ArrayList<>());

            // Update cashier balance for the specified currency
            for (Denomination deposit : depositDenominations) {
                Optional<Denomination> existingDenomination = cashierDenominations.stream()
                        .filter(d -> d.getValue() == deposit.getValue())
                        .findFirst();
                if (existingDenomination.isPresent()) {
                    // Update the quantity of the existing denomination
                    existingDenomination.get().setQuantity(
                            existingDenomination.get().getQuantity() + deposit.getQuantity()
                    );
                    existingDenomination.get().setTimestamp(
                            LocalDateTime.parse(LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER), LocalDateTimeFormatter.TIMESTAMP_FORMATTER)
                    );
                } else {
                    // Add new denomination entry
                    cashierDenominations.add(new Denomination(deposit.getQuantity(), deposit.getValue()));
                }
            }
        } finally {
            lock.unlock();
        }

        log.info("Deposit successful: {} {} deposit from cashier {}", request.getAmount(), request.getCurrency(), cashier.getName());
        //new Thread(() -> logTransaction("DEPOSIT", cashier.getName(), request)).start();
        //new Thread(this::logBalances).start();
        logging(cashier, request, "DEPOSIT");
    }


    private void withdraw(Cashier cashier, CashOperationRequest request) {
        log.info("Processing withdrawal of {} {} for cashier {}", request.getAmount(), request.getCurrency(), cashier.getName());

        Currency currency = request.getCurrency();
        ReentrantLock lock = getBalanceLock(cashier.getName(), currency);
        lock.lock();
        try {
            // Get the cashier's balance for the requested currency
            Map<Currency, List<Denomination>> balances = cashier.getBalances();
            List<Denomination> cashierDenominations = balances.get(currency);

            if (cashierDenominations == null) {
                log.error("Currency {} not supported for cashier {}", currency, cashier.getName());
                throw new CurrencyNotSupportedException(currency.toString());
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
                        // Subtract the requested quantity
                        cashierDenomination.setQuantity(cashierDenomination.getQuantity() - requestedDenomination.getQuantity());
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    log.error("Denomination {} not available for cashier {}", requestedDenomination.getValue(), cashier.getName());
                    throw new DenominationNotFoundException(requestedDenomination.getValue());
                }
            }

            // Update the cashier's balance
            balances.put(currency, updatedDenominations);
            cashier.setBalances(balances);
        } finally {
            lock.unlock();
        }

        log.info("Withdrawal successful: {} {} withdrawn from cashier {}", request.getAmount(), request.getCurrency(), cashier.getName());
        //new Thread(() -> logTransaction("WITHDRAW", cashier.getName(), request)).start();
        //new Thread(this::logBalances).start();
        logging(cashier, request, "WITHDRAWAL");
    }

    private void logging(Cashier cashier, CashOperationRequest request, String operation) {
        CompletableFuture<Void> transactionFuture = CompletableFuture.runAsync(() -> logTransaction(operation, cashier.getName(), request));
        CompletableFuture<Void> balanceFuture = CompletableFuture.runAsync(this::logBalances);
        try {
            transactionFuture.join(); // This will throw CompletionException with the cause
            balanceFuture.join(); // This will throw CompletionException with the cause
        } catch (CompletionException e) {
            if (e.getCause() instanceof LogTransactionException logTransactionException) {
                throw logTransactionException;
            } else if (e.getCause() instanceof LogBalancesException logBalancesException) {
                throw logBalancesException;
            } else {
                throw new LogTransactionException("Failed to log transaction.", e.getCause().getMessage());
            }
        }
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
        String timestamp = LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER);
        CompletableFuture<Void> transactionLogFuture =
                CompletableFuture
                        .supplyAsync(() ->
                                String.format("%s - %s: %s %s%n", timestamp, operation, cashierName, request)
                        )
                        .thenCompose(content ->
                                CompletableFuture.runAsync(() -> {
                                    try {
                                        Files.write(Paths.get(CashierRepository.TRANSACTION_FILE), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                                        //throw new IOException("Failed to log transaction."); // simulate transaction log error
                                    } catch (Exception e) {
                                        log.error("Failed to log transaction", e);
                                        throw new LogTransactionException("Failed to log transaction.", e.getMessage());
                                    }
                                })
                        );

        // Wait for completion and propagate any exception
        transactionLogFuture.join();
    }

    public void logBalances() {
        String timestamp = LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER);
        CompletableFuture<Void> balanceLogFuture =
                CompletableFuture
                        .supplyAsync(() -> CashierRepository.CASHIERS.entrySet()
                                .parallelStream()
                                .map(entry ->
                                        timestamp + " - " +
                                                entry.getKey() + ": " +
                                                entry.getValue()
                                                        .getBalances().entrySet().stream()
                                                        .collect(Collectors.toMap
                                                                (
                                                                        Map.Entry::getKey,
                                                                        value -> value.getValue().stream()
                                                                                .filter(d -> d.getQuantity() != 0)
                                                                                .toList(),
                                                                        (a, b) -> a, // merge function - shouldn't be needed for EnumMap
                                                                        () -> new EnumMap<>(Currency.class)
                                                                )
                                                        ) +
                                                "\n"
                                )
                                .collect(Collectors.joining())
                        )
                        .thenCompose(content ->
                                CompletableFuture.runAsync(() -> {
                                    try {
                                        Files.write(Paths.get(CashierRepository.BALANCE_FILE), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                                        //throw new IOException("Failed to log balances"); // simulate balances log error
                                    } catch (Exception e) {
                                        log.error("Failed to write balance log file", e);
                                        throw new LogBalancesException("Failed to log balances.", e.getMessage());
                                    }
                                })
                        );

        // Wait for completion and propagate any exception
        balanceLogFuture.join();
    }
}
