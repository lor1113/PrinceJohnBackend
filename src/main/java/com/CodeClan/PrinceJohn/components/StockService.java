package com.CodeClan.PrinceJohn.components;

import com.CodeClan.PrinceJohn.models.Stock;
import com.CodeClan.PrinceJohn.models.StockMetadata;
import com.CodeClan.PrinceJohn.repositories.StockMetadataRepository;
import com.CodeClan.PrinceJohn.repositories.StockRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.Period;
import java.util.Iterator;
import java.util.Optional;

@Component
@EnableScheduling
public class StockService {
    @Autowired
    StockRepository stockRepository;

    @Autowired
    StockMetadataRepository stockMetadataRepository;
    String nasdaqApiKey = "WW8WRVpoAFoz9VnjyJzB";
    String nasdaqUrlBase = "https://data.nasdaq.com/api/v3/datasets/WIKI/%s.json?column_index=4&start_date=%s&end_date=%s&api_key=%s";
    int dayShift = 2997;

    private StockMetadata newStockMetadata() {
        System.out.println("Stock metadata not found, saving new metadata.");
        LocalDate lastUpdated = LocalDate.of(2022, 9, 1);
        StockMetadata stockMetadata = new StockMetadata(1L, lastUpdated);
        stockMetadataRepository.save(stockMetadata);
        return stockMetadata;
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void loadStockData() {
        System.out.println("Getting stock data");
        StockMetadata stockMetadata = stockMetadataRepository.findById(1L).orElseGet(this::newStockMetadata);
        LocalDate now = LocalDate.now();
        LocalDate tomorrow = now.plusDays(1);
        LocalDate yesterday = now.minusDays(1);
        LocalDate lastUpdated = stockMetadata.lastUpdated;
        if (now.equals(lastUpdated)) {
            System.out.println("Stocks already up to date");
        } else {
            System.out.println("Updating stocks");
            WebClient client = WebClient.create();
            LocalDate adjustedNow = now.minusDays(dayShift);
            LocalDate adjustedLastUpdated = lastUpdated.minusDays(dayShift);
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            for (String ticker : stockMetadata.tickerList) {
                Stock stock = stockRepository.findByTicker(ticker).orElse(new Stock(ticker, 0F));
                String url = String.format(nasdaqUrlBase, ticker, adjustedLastUpdated, adjustedNow, nasdaqApiKey);
                String responseString = client.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                JsonNode jsonNode;
                try {
                    jsonNode = mapper.readTree(responseString);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                JsonNode data = jsonNode.get("dataset").get("data");
                Iterator<JsonNode> elements = data.elements();
                while (elements.hasNext()) {
                    ArrayNode node = (ArrayNode) elements.next();
                    String dateString = node.get(0).toString().replaceAll("\"", "");
                    LocalDate priceDate = LocalDate.parse(dateString);
                    LocalDate adjustedPriceDate = priceDate.plus(Period.ofDays(dayShift));
                    Float price = node.get(1).floatValue();
                    if (now.equals(adjustedPriceDate)) {
                        stock.price = price;
                    }
                    stock.priceHistory.put(adjustedPriceDate, price);
                }
                Float lastSeen = Optional.ofNullable(stock.priceHistory.get(stockMetadata.lastUpdated)).orElse(0F);
                for (lastUpdated = stockMetadata.lastUpdated; lastUpdated.isBefore(tomorrow); lastUpdated = lastUpdated.plusDays(1)) {
                    Optional<Float> stockPrice = Optional.ofNullable(stock.priceHistory.get(lastUpdated));
                    if (stockPrice.isPresent()) {
                        lastSeen = stockPrice.get();
                    } else {
                        if (now.equals(lastUpdated)) {
                            stock.price = lastSeen;
                        } else {
                            stock.priceHistory.put(lastUpdated, lastSeen);
                        }
                    }
                }
                Float yesterdayPrice = stock.priceHistory.get(yesterday);
                stock.delta = ((stock.price - yesterdayPrice) / yesterdayPrice) * 100;
                stockRepository.save(stock);
            }
            stockMetadata.lastUpdated = now;
            stockMetadataRepository.save(stockMetadata);
            System.out.println("Successfully updated stocks");
        }

    }


}
