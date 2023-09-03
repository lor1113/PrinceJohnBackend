package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "stock_metadata")
public class StockMetadata {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<String> getTickerList() {
        return tickerList;
    }

    public void setTickerList(List<String> tickerList) {
        this.tickerList = tickerList;
    }

    @Id
    public Long id;
    public LocalDate lastUpdated;
    @ElementCollection
    public List<String> tickerList;

    public StockMetadata(Long id, LocalDate lastUpdated, List<String> tickerList) {
        this.id = id;
        this.lastUpdated = lastUpdated;
        this.tickerList = tickerList;
    }

    public StockMetadata () {};

}
