package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.models.Stock;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.models.UserTransaction;
import com.CodeClan.PrinceJohn.repositories.StockRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint/s3")
public class AppEndpointControllerS3 {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StockRepository stockRepository;

    @PostMapping("/userTransaction")
    public ResponseEntity<?> userTransact(@RequestBody UserTransaction newUserTransaction) {
        Optional<User> userOut = userRepository.findById(newUserTransaction.id);
        if (userOut.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Optional<Stock> stockOut = stockRepository.findByTicker(newUserTransaction.stockTicker);
        if (stockOut.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = userOut.get();
        Stock stock = stockOut.get();
        Boolean success;
        if (newUserTransaction.buy) {
            success = user.buyStock(stock, newUserTransaction.amount);
        } else {
            success = user.sellStock(stock, newUserTransaction.amount);
        }
        if (success) {
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
