package com.example.cashoperations.utils;

import com.example.cashoperations.exception.LogBalancesException;
import com.example.cashoperations.exception.LogTransactionException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class BatchFileWriter {
    private final BlockingQueue<String> transactionQueue = new LinkedBlockingQueue<>(100000);
    private final BlockingQueue<String> balanceQueue = new LinkedBlockingQueue<>(10000);
    private final ScheduledExecutorService batchScheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Path TRANSACTION_FILE = Paths.get("transactions.log");
    private static final Path BALANCE_FILE = Paths.get("balances.log");
    private static final int BATCH_SIZE = 1000;
    private static final int BATCH_INTERVAL_MS = 100;

    @PostConstruct
    public void init() {
        // Schedule batch flushing
        batchScheduler.scheduleAtFixedRate(this::flushTransactionBatch, BATCH_INTERVAL_MS, BATCH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        batchScheduler.scheduleAtFixedRate(this::flushBalanceBatch, BATCH_INTERVAL_MS * 2, BATCH_INTERVAL_MS * 2, TimeUnit.MILLISECONDS);
    }

    public void enqueueTransaction(String content) {
        transactionQueue.offer(content);
        if (transactionQueue.size() >= BATCH_SIZE) {
            batchScheduler.execute(this::flushTransactionBatch);
        }
    }

    public void enqueueBalance(String content) {
        balanceQueue.offer(content);
        if (balanceQueue.size() >= BATCH_SIZE / 10) { // Smaller batches for balances
            batchScheduler.execute(this::flushBalanceBatch);
        }
    }

    private void flushTransactionBatch() {
        flushBatch(transactionQueue, TRANSACTION_FILE, "transaction");
    }

    private void flushBalanceBatch() {
        flushBatch(balanceQueue, BALANCE_FILE, "balance");
    }

    @Async("ioExecutor")
    void flushBatch(BlockingQueue<String> queue, Path file, String type) {
        if (queue.isEmpty()) return;

        List<String> batch = new ArrayList<>(BATCH_SIZE);
        queue.drainTo(batch, BATCH_SIZE);

        if (!batch.isEmpty()) {
            try {
                String batchContent = String.join("", batch);
                Files.write(file, batchContent.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                throw new IOException(String.join("Failed to write {} batch", type)); // simulate transaction log error
            } catch (Exception e) {
                log.error("Failed to write {} batch", type, e);
                if (type.equals("transaction")) {
                    throw new LogTransactionException("Failed to log transaction.", e.getMessage());
                }
                if (type.equals("balance")) {
                    throw new LogBalancesException("Failed to log balance.", e.getMessage());
                }
            }
        }
    }

/*    @Async("ioExecutor") // Using CompletableFuture
    void flushBatch(BlockingQueue<String> queue, Path file, String type) {
        if (queue.isEmpty()) return;

        List<String> batch = new ArrayList<>(BATCH_SIZE);
        queue.drainTo(batch, BATCH_SIZE);

        if (!batch.isEmpty()) {
            CompletableFuture<Void> future =
                    CompletableFuture.runAsync(() -> {
                                try {
                                    String batchContent = String.join("", batch);
                                    Files.write(file, batchContent.getBytes(StandardCharsets.UTF_8),
                                            StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                                    throw new IOException("Failed to log transaction."); // simulate transaction log error
                                } catch (Exception e) {
                                    log.error("Failed to write {} batch", type, e);
                                    if (type.equals("transaction")) {
                                        throw new CompletionException(new LogTransactionException("Failed to log transaction.", e.getMessage()));
                                    }
                                    if (type.equals("balance")) {
                                        throw new CompletionException(new LogBalancesException("Failed to log balance.", e.getMessage()));
                                    }
                                }
                            })
                            .exceptionally(ex -> {
                                throw new CompletionException(ex);// triggers @ExceptionHandler
                            });

            try {
                future.join();
            } catch (CompletionException ex) {
                Throwable cause = NestedExceptionUtils.getRootCause(ex);
                if (cause instanceof LogTransactionException logEx) {
                    throw logEx; // direct rethrow
                } else if (cause instanceof LogBalancesException balEx) {
                    throw balEx; // direct rethrow
                } else if (cause instanceof RuntimeException re) {
                    throw re; // other runtime exception
                }
                throw new RuntimeException("Unexpected async error", ex);
            }
        }
    }*/

    @PreDestroy
    public void shutdown() {
        batchScheduler.shutdown();
        // Flush remaining messages
        flushTransactionBatch();
        flushBalanceBatch();
    }
}