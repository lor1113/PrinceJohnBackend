package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Entity
@Table(name = "users")
public class User {

    public String username;
    public LocalDate birthday;
    @Column(unique = true)
    public String email;
    @ElementCollection
    @MapKeyColumn(name = "stock_ticker")
    @Column(name = "quantity")
    public Map<String, Integer> portfolio;
    @ElementCollection
    @MapKeyColumn(name = "value_date")
    @Column(name = "value_on_date")
    public Map<LocalDate, Float> portfolioValueHistory;
    public float balance;
    public long lastTransaction;
    public LocalDate accountCreated;
    public LocalDate portfolioHistoryUpdated;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public User(String username, String email, LocalDate birthday) {
        this.username = username;
        this.birthday = birthday;
        this.email = email.toLowerCase();
        this.balance = 0F;
        this.portfolio = new HashMap<>();
        this.portfolioValueHistory = new HashMap<>();
        this.accountCreated = LocalDate.now();
        this.portfolioHistoryUpdated = LocalDate.now();
        this.lastTransaction = 0L;
    }

    public User(ProspectiveUser prospectiveUser) {
        this.username = prospectiveUser.username;
        this.birthday = prospectiveUser.birthday;
        this.email = prospectiveUser.email;
        this.balance = 0;
        this.portfolio = new HashMap<>();
        this.portfolioValueHistory = new HashMap<>();
        this.accountCreated = LocalDate.now();
        this.portfolioHistoryUpdated = LocalDate.now();
        this.lastTransaction = 0L;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public Map<String, Integer> getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Map<String, Integer> portfolio) {
        this.portfolio = portfolio;
    }

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

    public void addBalance(float deposit) {
        this.balance = this.balance + deposit;
    }

    public LocalDate getAccountCreated() {
        return accountCreated;
    }

    public void setAccountCreated(LocalDate accountCreated) {
        this.accountCreated = accountCreated;
    }

    public Boolean buyStock(Map<Stock, Integer> orderList) {
        float totalPrice = 0F;
        for (Map.Entry<Stock, Integer> entry : orderList.entrySet()) {
            Stock stock = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity < 1) {
                return Boolean.FALSE;
            }
            totalPrice += (stock.price * quantity);
        }
        if (totalPrice <= this.balance) {
            this.balance -= totalPrice;
            for (Map.Entry<Stock, Integer> entry : orderList.entrySet()) {
                Stock stock = entry.getKey();
                Integer quantity = entry.getValue();
                int owned = Optional.ofNullable(portfolio.get(stock.ticker)).orElse(0);
                portfolio.put(stock.ticker, owned + quantity);
            }
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Boolean sellStock(Map<Stock, Integer> orderList) {
        boolean canSell = Boolean.TRUE;
        float totalPrice = 0F;
        for (Map.Entry<Stock, Integer> entry : orderList.entrySet()) {
            Stock stock = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity < 1) {
                return Boolean.FALSE;
            }
            int owned = Optional.ofNullable(portfolio.get(stock.ticker)).orElse(0);
            if (owned <= quantity) {
                canSell = Boolean.FALSE;
            }
            totalPrice += (stock.price * quantity);
        }
        if (canSell) {
            for (Map.Entry<Stock, Integer> entry : orderList.entrySet()) {
                Stock stock = entry.getKey();
                Integer quantity = entry.getValue();
                int owned = Optional.ofNullable(portfolio.get(stock.ticker)).orElse(0);
                portfolio.put(stock.ticker, owned - quantity);
            }
            this.balance += totalPrice;
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public Boolean userWithdraw(Integer amount) {
        if (amount > this.balance) {
            return Boolean.FALSE;
        } else {
            this.balance -= amount;
            return Boolean.TRUE;
        }
    }

    public Boolean userDeposit(Integer amount) {
        this.balance += amount;
        return Boolean.TRUE;
    }
}
