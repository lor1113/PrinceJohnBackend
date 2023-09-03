package com.CodeClan.PrinceJohn.components;

import com.CodeClan.PrinceJohn.models.Stock;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.repositories.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Component
public class UserService {

    @Autowired
    StockRepository stockRepository;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private User user;

    public int portfolioValue () {
        float total = 0;
        for (Map.Entry<String, Integer> entry : this.user.portfolio.entrySet()) {
            String stockTicker = entry.getKey();
            Stock stock = stockRepository.findByTicker(stockTicker).orElseThrow();
            Integer quantity = entry.getValue();
            total += (stock.price * quantity);
        }
        return Math.round(total);
    };

    public void updatePortfolioHistory() {
        LocalDate now = LocalDate.now();
        LocalDate lastUpdated = user.portfolioHistoryUpdated;
        if (now.isAfter(lastUpdated)){
            for (Map.Entry<String, Integer> entry : this.user.portfolio.entrySet()) {
                String stockTicker = entry.getKey();
                Stock stock = stockRepository.findByTicker(stockTicker).orElseThrow();
                Integer quantity = entry.getValue();
                for (lastUpdated = user.portfolioHistoryUpdated;lastUpdated.isBefore(now);) {
                    lastUpdated = lastUpdated.plusDays(1);
                    Float stockPrice = stock.priceHistory.get(lastUpdated);
                    Float interim = Optional.ofNullable(user.portfolioValueHistory.get(lastUpdated)).orElse(0F);
                    Float newTotal = (stockPrice * quantity) + interim;
                    user.portfolioValueHistory.put(lastUpdated, newTotal);
                }
            }
            user.portfolioHistoryUpdated = now;
        }
    }

}
