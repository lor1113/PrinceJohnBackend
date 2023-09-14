package com.CodeClan.PrinceJohn.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
@Profile("!test")
public class BootService {
    @Autowired
    StockService stockService;

    String cloudflareURLBase = "https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s";

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        stockService.loadStockData();
        updateDNS();
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void updateDNS () {
        Properties appProps = new Properties();
        String localAddress;
        System.out.println("Checking current IP address");
        try {
            appProps.load(new FileInputStream("src/main/resources/application.properties"));
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            localAddress = socket.getLocalAddress().toString().replace("/","");
            socket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String lastAddr = appProps.getProperty("princeJohn.lastIP");
        if (!localAddress.equals(lastAddr)) {
            System.out.println("IP Address changed: setting new IP");
            String password = appProps.getProperty("princeJohn.DDNSpass");
            String zoneID = appProps.getProperty("princeJohn.zoneID");
            String recordID = appProps.getProperty("princeJohn.recordID");
            String domain = appProps.getProperty("princeJohn.domain");
            WebClient client = WebClient.create();
            Map<String, String> body = new HashMap<>();
            body.put("content",localAddress);
            body.put("name",domain);
            body.put("type","A");
            String url = String.format(cloudflareURLBase,zoneID,recordID);
            Mono<Boolean> request = client.put()
                    .uri(url)
                    .header("Content-Type","application/json")
                    .header("Authorization","Bearer " + password)
                    .bodyValue(body)
                    .exchangeToMono(response -> {
                        if (response.statusCode().equals(HttpStatus.OK)) {
                            return Mono.just(Boolean.TRUE);
                        }
                        else {
                            return Mono.just(Boolean.FALSE);
                        }
                    });
            Boolean success = request.block();
            if (success) {
                try {
                    appProps.setProperty("princeJohn.lastIP",localAddress);
                    appProps.store(new FileOutputStream("src/main/resources/application.properties"),null);
                    System.out.println("Changed dynamic DNS IP Address to " + localAddress);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Failed to update dynamic DNS");
            }

        } else {
            System.out.println("IP Address unchanged");
        }
    }
}
