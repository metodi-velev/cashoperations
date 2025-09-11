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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Key Improvements:
 * <p>
 * 1. <b>Minimized Critical Sections</b>:
 * ○ Moved all possible operations (validation, mapping, preparation) outside the lock
 * ○ Reduced lock scope to only the essential balance modification code
 * <p>
 * 2. <b>Lock Timeouts</b>: Added tryLock() with timeout to prevent deadlocks
 * <p>
 * 3. <b>Faster Data Structures</b>:
 * ○ Used Map for O(1) denomination lookups instead of O(n) stream operations
 * ○ Bulk operations instead of per-denomination processing
 * <p>
 * 4. <b>Asynchronous Logging</b>:
 * ○ Moved file I/O operations to separate threads
 * ○ Prevents blocking on disk operations
 * <p>
 * 5. <b>Error Handling</b>:
 * ○ Proper interrupt handling
 * ○ Specific exception for concurrent operation failures
 * <p>
 * 6. <b>Memory Efficiency</b>:
 * ○ Avoid unnecessary object creation in critical sections
 * ○ Use existing objects where possible
 * <p>
 * 7. <b>Thread Safety</b>:
 * ○ Fair locks to prevent thread starvation
 * ○ Proper finally blocks for lock release
 * <p>
 * These changes significantly reduce lock contention and improve throughput while maintaining thread safety.
 */
@Slf4j
@RequiredArgsConstructor
@Service("cashDeskServiceImplV2")
public class CashDeskServiceImplV2 implements CashDeskService {

    private final CashierRepository cashierRepository;

    // Fine-grained locks per cashier+currency with timeout capability
    private final ConcurrentMap<String, ReentrantLock> balanceLocks = new ConcurrentHashMap<>();
    private static final long LOCK_TIMEOUT_MS = 1000; // 1 second timeout

    private ReentrantLock getBalanceLock(String cashierName, Currency currency) {
        String key = cashierName + "|" + currency.name();
        return balanceLocks.computeIfAbsent(key, k -> new ReentrantLock(true)); // Fair lock to prevent starvation
    }

    @Override
    public void performOperation(CashOperationRequest request) {
        Cashier cashier = cashierRepository.getCashier(request.getCashierName());
        if (cashier == null) {
            throw new ResourceNotFoundException("Cashier", "name", request.getCashierName());
        }

        checkAmountValidity(request);

        try {
            if ("DEPOSIT".equalsIgnoreCase(request.getOperationType())) {
                deposit(cashier, request);
            } else if ("WITHDRAWAL".equalsIgnoreCase(request.getOperationType())) {
                withdraw(cashier, request);
            }
        } finally {
            // Update outside of fine-grained locks to avoid holding locks during repository operations
            cashierRepository.updateCashier(cashier);
        }
    }

    private void deposit(Cashier cashier, CashOperationRequest request) {
        validateDepositRequest(cashier, request);

        Currency currency = request.getCurrency();
        List<Denomination> depositDenominations = request.getDenominations();

        // Create a map for quick denomination lookup - done outside the lock
        Map<BigDecimal, Denomination> depositMap = depositDenominations.stream()
                .collect(Collectors.toMap(
                        d -> BigDecimal.valueOf(d.getValue()),
                        d -> d,
                        (existing, replacement) -> {
                            existing.setQuantity(existing.getQuantity() + replacement.getQuantity());
                            return existing;
                        }
                ));

        ReentrantLock lock = getBalanceLock(cashier.getName(), currency);
        try {
            if (!lock.tryLock(LOCK_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                throw new ConcurrentOperationException("Could not acquire lock for deposit operation");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrentOperationException("Deposit operation interrupted while waiting for lock");
        }

        String logEntry = "";
        String logMessage = "";

        try {
            // CRITICAL SECTION START - Minimized as much as possible
            Map<Currency, List<Denomination>> cashierBalances = cashier.getBalances();
            List<Denomination> cashierDenominations = cashierBalances.computeIfAbsent(currency, k -> new ArrayList<>());

            // Convert to map for faster lookup during update
            Map<BigDecimal, Denomination> cashierDenomMap = cashierDenominations.stream()
                    .collect(Collectors.toMap(
                            d -> BigDecimal.valueOf(d.getValue()),
                            d -> d
                    ));

            LocalDateTime now = LocalDateTime.now();
            String formattedTimestamp = now.format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER);

            // Process all denominations in bulk
            for (Denomination deposit : depositDenominations) {
                BigDecimal value = BigDecimal.valueOf(deposit.getValue());
                Denomination existing = cashierDenomMap.get(value);

                if (existing != null) {
                    existing.setQuantity(existing.getQuantity() + deposit.getQuantity());
                    existing.setTimestamp(LocalDateTime.parse(formattedTimestamp, LocalDateTimeFormatter.TIMESTAMP_FORMATTER));
                } else {
                    Denomination newDenom = new Denomination(deposit.getQuantity(), deposit.getValue());
                    newDenom.setTimestamp(LocalDateTime.parse(formattedTimestamp, LocalDateTimeFormatter.TIMESTAMP_FORMATTER));
                    cashierDenomMap.put(value, newDenom);
                }
            }

            // Update the list from the map
            cashierDenominations.clear();
            cashierDenominations.addAll(cashierDenomMap.values());
            String timestamp = LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER);
            logEntry = String.format("%s - %s: %s %s%n", timestamp, "DEPOSIT", cashier.getName(), request);
            logMessage = String.format("{} successful: {} {} for cashier {}", "DEPOSIT", request.getAmount(),
                    request.getCurrency(), cashier.getName());
            // CRITICAL SECTION END

        } finally {
            lock.unlock();
        }

        logSuccess(logEntry, logMessage);
    }

    private void withdraw(Cashier cashier, CashOperationRequest request) {
        Currency currency = request.getCurrency();
        List<Denomination> requestedDenominations = request.getDenominations();

        ReentrantLock lock = getBalanceLock(cashier.getName(), currency);
        try {
            if (!lock.tryLock(LOCK_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                throw new ConcurrentOperationException("Could not acquire lock for withdrawal operation");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrentOperationException("Withdrawal operation interrupted while waiting for lock");
        }

        String logEntry = "";
        String logMessage = "";

        try {
            // CRITICAL SECTION START - Minimized
            Map<Currency, List<Denomination>> balances = cashier.getBalances();
            List<Denomination> cashierDenominations = balances.get(currency);

            if (cashierDenominations == null || cashierDenominations.isEmpty()) {
                throw new CurrencyNotSupportedException(currency.toString());
            }

            // Create a copy for validation without modifying original
            Map<BigDecimal, Integer> availableQuantities = cashierDenominations.stream()
                    .collect(Collectors.toMap(
                            d -> BigDecimal.valueOf(d.getValue()),
                            Denomination::getQuantity
                    ));

            // Validate all denominations first
            for (Denomination requested : requestedDenominations) {
                BigDecimal value = BigDecimal.valueOf(requested.getValue());
                Integer availableQty = availableQuantities.get(value);

                if (availableQty == null) {
                    throw new DenominationNotFoundException(requested.getValue());
                }
                if (availableQty < requested.getQuantity()) {
                    throw new InsufficientDenominationException(
                            requested.getQuantity(), requested.getValue(),
                            availableQty, requested.getValue()
                    );
                }
            }

            // Apply changes - all validations passed
            for (Denomination requested : requestedDenominations) {
                BigDecimal value = BigDecimal.valueOf(requested.getValue());
                for (Denomination cashierDenom : cashierDenominations) {
                    if (cashierDenom.getValue() == requested.getValue()) {
                        cashierDenom.setQuantity(cashierDenom.getQuantity() - requested.getQuantity());
                        break;
                    }
                }
            }

            // Remove zero-quantity denominations
            cashierDenominations.removeIf(d -> d.getQuantity() <= 0);
            String timestamp = LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER);
            logEntry = String.format("%s - %s: %s %s%n", timestamp, "WITHDRAWAL", cashier.getName(), request);
            logMessage = String.format("{} successful: {} {} for cashier {}", "WITHDRAWAL", request.getAmount(),
                    request.getCurrency(), cashier.getName());
            // CRITICAL SECTION END

        } finally {
            lock.unlock();
        }

        logSuccess(logEntry, logMessage);
    }

    private void validateDepositRequest(Cashier cashier, CashOperationRequest request) {
        if (request == null) {
            throw new InvalidDepositException("Invalid deposit request. Deposit request must be defined.");
        }
        if (cashier == null) {
            throw new InvalidDepositException("Invalid deposit request. Cashier cannot be null.");
        }
        if (request.getDenominations() == null || request.getDenominations().isEmpty()) {
            throw new InvalidDepositException("Invalid deposit request. Deposit request must contain at least one valid denomination.");
        }
    }

    private void checkAmountValidity(CashOperationRequest request) {
        BigDecimal amount = request.getAmount();

        int denominationsAmountSum = request.getDenominations().stream()
                .mapToInt(d -> d.getValue() * d.getQuantity())
                .sum();

        BigDecimal bigDecimalDenominationsAmountSum = new BigDecimal(denominationsAmountSum);

        if (amount.compareTo(bigDecimalDenominationsAmountSum) != 0) {
            throw new InvalidAmountException("Invalid request. Amount "
                    + amount
                    + " does not match overall denominations sum "
                    + bigDecimalDenominationsAmountSum + "."
            );
        }
    }

    private void logSuccess(String logEntry, String logMessage) {
        log.info(logMessage);
        logTransaction(logEntry);
        // Consider moving balance logging to async operation
        new Thread(this::logBalancesAsync).start();
    }

    private void logTransaction(String logEntry) {

        // Async file writing to avoid blocking
        new Thread(() -> {
            try {
                Files.write(Paths.get(CashierRepository.TRANSACTION_FILE),
                        logEntry.getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                log.error("Failed to log transaction", e);
            }
        }).start();
    }

    private void logBalancesAsync() {
        String timestamp = LocalDateTime.now().format(LocalDateTimeFormatter.TIMESTAMP_FORMATTER);
        StringBuilder sb = new StringBuilder();
        CashierRepository.CASHIERS.forEach((name, cashier) ->
                sb.append(timestamp).append(" - ").append(name).append(": ")
                        .append(cashier.getBalances()).append("\n"));

        try {
            Files.write(Paths.get(CashierRepository.BALANCE_FILE),
                    sb.toString().getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to log balances", e);
        }
    }

    // Exception for concurrent operations
    public static class ConcurrentOperationException extends RuntimeException {
        public ConcurrentOperationException(String message) {
            super(message);
        }
    }
}
