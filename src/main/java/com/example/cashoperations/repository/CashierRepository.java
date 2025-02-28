package com.example.cashoperations.repository;

import com.example.cashoperations.model.Cashier;
import com.example.cashoperations.model.Currency;
import com.example.cashoperations.model.Denomination;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CashierRepository {
    public static final Map<String, Cashier> CASHIERS = new HashMap<>();
    public static final String TRANSACTION_FILE = "transactions.txt";
    public static final String BALANCE_FILE = "balances.txt";

    @PostConstruct
    public void init() {
        CASHIERS.put("MARTINA", createCashier("MARTINA", 1000, 2000));
        CASHIERS.put("PETER", createCashier("PETER", 1000, 2000));
        CASHIERS.put("LINDA", createCashier("LINDA", 1000, 2000));
    }

    private Cashier createCashier(String name, int bgnBalance, int eurBalance) {
        Map<Currency, List<Denomination>> balances = new HashMap<>();
        balances.put(Currency.BGN, createDenominations(bgnBalance, Map.of(50, 10, 10, 50)));
        balances.put(Currency.EUR, createDenominations(eurBalance, Map.of(100, 10, 20, 50)));
        return new Cashier(name, balances);
    }

    private List<Denomination> createDenominations(int totalAmount, Map<Integer, Integer> values) {
        return values.entrySet().stream()
                .map(e -> new Denomination(e.getKey(), e.getValue(), totalAmount))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Cashier getCashier(String name) {
        return CASHIERS.get(name);
    }

    public void updateCashier(Cashier cashier) {
        CASHIERS.put(cashier.getName(), cashier);
    }
}
