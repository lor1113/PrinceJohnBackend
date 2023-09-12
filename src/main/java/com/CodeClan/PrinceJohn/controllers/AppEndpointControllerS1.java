package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.UserService;
import com.CodeClan.PrinceJohn.models.Stock;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.repositories.StockRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/appEndpoint/s1")
public class AppEndpointControllerS1 {
    @Autowired
    StockRepository stockRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;


    @GetMapping("/stockData/{date}")
    public ResponseEntity<List<Stock>> getStocks(@PathVariable(required = false) Date date) {
        List<Stock> stockList = stockRepository.findAll();
        if (date != null){
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            for (Stock stock : stockList) {
                Map<LocalDate,Float> result = stock.priceHistory.entrySet()
                        .stream()
                        .filter(map -> map.getKey().isAfter(localDate))
                        .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
                stock.priceHistory = result;
            }
        }
        return new ResponseEntity<>(stockList, HttpStatus.OK);
    }

    @GetMapping("/userData")
    public ResponseEntity<User> getUser(Principal principal) {
        Long id = Long.valueOf(principal.getName());
        Optional<User> userOut = userRepository.findById(id);
        if (userOut.isPresent()) {
            try {
                User user = userOut.get();
                userService.setUser(user);
                userService.updatePortfolioHistory();
                User updatedUser = userService.getUser();
                userRepository.save(updatedUser);
                return new ResponseEntity<>(updatedUser, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Long> testFunction(Principal principal) {
        Long id = Long.valueOf(principal.getName());
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

}
