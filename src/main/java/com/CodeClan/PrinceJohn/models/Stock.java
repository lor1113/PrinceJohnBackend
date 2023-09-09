package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "stocks")
public class Stock {

    @ElementCollection
    @MapKeyColumn(name = "price_date")
    @Column(name = "price_on_date")
    public Map<LocalDate, Float> priceHistory;
    @Id
    public String ticker;
    public Float price;

    public Stock(String ticker, Float price) {
        this.ticker = ticker;
        this.price = price;
        this.priceHistory = new HashMap<>();
    }

    public Stock() {
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Map<LocalDate, Float> getPriceHistory() {
        return priceHistory;
    }

    public void setPriceHistory(Map<LocalDate, Float> priceHistory) {
        this.priceHistory = priceHistory;
    }
}
