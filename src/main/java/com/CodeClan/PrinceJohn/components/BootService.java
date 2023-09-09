package com.CodeClan.PrinceJohn.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class BootService {
    @Autowired
    StockService stockService;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        stockService.loadStockData();
    }
}
