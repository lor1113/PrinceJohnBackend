package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "users")
public class User {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    public String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public LocalDate birthday;
    public String email;

    public Map<String, Integer> getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Map<String, Integer> portfolio) {
        this.portfolio = portfolio;
    }

    @ElementCollection
    public Map<String, Integer> portfolio;

    public Map<LocalDate, Float> getPortfolioValueHistory() {
        return portfolioValueHistory;
    }

    public void setPortfolioValueHistory(Map<LocalDate, Float> portfolioValueHistory) {
        this.portfolioValueHistory = portfolioValueHistory;
    }

    public LocalDate getPortfolioHistoryUpdated() {
        return portfolioHistoryUpdated;
    }

    public void setPortfolioHistoryUpdated(LocalDate portfolioHistoryUpdated) {
        this.portfolioHistoryUpdated = portfolioHistoryUpdated;
    }

    @ElementCollection
    public Map<LocalDate, Float> portfolioValueHistory;
    public int balance;

    public User(String username, String email, LocalDate birthday) {
        this.username = username;
        this.birthday = birthday;
        this.email = email;
        this.balance = 0;
        this.portfolio = new HashMap<>();
        this.portfolioValueHistory = new HashMap<>();
        this.accountCreated = LocalDate.now();
        this.portfolioHistoryUpdated = LocalDate.now();
    }

    public User () {}

    public void addBalance (int deposit) {this.balance = this.balance + deposit;}

    public LocalDate getAccountCreated() {
        return accountCreated;
    }

    public void setAccountCreated(LocalDate accountCreated) {
        this.accountCreated = accountCreated;
    }

    public LocalDate accountCreated;

    public LocalDate portfolioHistoryUpdated;

    public Boolean buyStock(Stock stock, int quantity) {
        Float totalPrice = stock.price * quantity;
        if (totalPrice <= this.balance) {
            this.balance -= totalPrice;
            int owned = Optional.ofNullable(portfolio.get(stock.ticker)).orElse(0);
            portfolio.put(stock.ticker, owned + quantity);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Boolean sellStock(Stock stock, int quantity) {
        int owned = Optional.ofNullable(portfolio.get(stock.ticker)).orElse(0);
        if (owned >= quantity) {
            Float totalPrice = stock.price * quantity;
            portfolio.put(stock.ticker, owned - quantity);
            this.balance += totalPrice;
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Boolean transact(Stock stock, int quantity, Boolean buy) {
        if (buy) {
            return buyStock(stock, quantity);
        } else {
            return sellStock(stock, quantity);
        }
    }
}
