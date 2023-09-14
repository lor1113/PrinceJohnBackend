package com.CodeClan.PrinceJohn.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_metadata")
public class StockMetadata {
    public LocalDate lastUpdated;
    @ElementCollection
    public List<String> tickerList;

    @ElementCollection
    public List<StockBaseData> stockBaseDataList;

    @Id
    private Long id;

    public StockMetadata(Long id, LocalDate lastUpdated) {
        this.id = id;
        this.lastUpdated = lastUpdated;
        try {
            ObjectMapper mapper = new ObjectMapper();
            File baseFile = new File("src/main/resources/stockBaseData/stockBaseData.json");
            List<StockBaseData> baseList = mapper.readValue(baseFile, new TypeReference<List<StockBaseData>>(){});
            this.stockBaseDataList = baseList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.tickerList = new ArrayList<>();
        for (StockBaseData data : this.stockBaseDataList) {
            this.tickerList.add(data.ticker);
        }
    }

    public StockMetadata() {}

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

}
