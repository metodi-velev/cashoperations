package com.example.cashoperations.model;

import java.util.List;
import java.util.Map;

public class Cashier {
    private String name;
    private Map<Currency, List<Denomination>> balances;

    public Cashier() {
    }

    public Cashier(String name, Map<Currency, List<Denomination>> balances) {
        this.name = name;
        this.balances = balances;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Currency, List<Denomination>> getBalances() {
        return balances;
    }

    public void setBalances(Map<Currency, List<Denomination>> balances) {
        this.balances = balances;
    }

    public void addBalance(Currency currency, List<Denomination> denominations) {
        this.getBalances().put(currency, denominations);
    }
}
