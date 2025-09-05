package com.example.cashoperations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("ioExecutor")
    public Executor ioBoundTaskExecutor() {
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("io-pool-" + thread.getId());
            return thread;
        });
    }

    @Bean("cpuExecutor")
    @Primary
    public Executor cpuBoundTaskExecutor() {
        return Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
    }
}
