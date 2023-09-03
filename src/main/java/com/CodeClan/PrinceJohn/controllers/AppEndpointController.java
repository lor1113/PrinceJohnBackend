package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.UserService;
import com.CodeClan.PrinceJohn.models.Stock;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.models.UserTransaction;
import com.CodeClan.PrinceJohn.repositories.StockRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint")
public class AppEndpointController {

    @Autowired
    StockRepository stockRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @GetMapping("/stockData")
    public ResponseEntity<List<Stock>> getStocks () {
        return new ResponseEntity<>(stockRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/userData/{id}")
    public ResponseEntity<User> getUser (@PathVariable Long id) {
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
    @PostMapping("/userTransaction")
    public ResponseEntity<?> userTransact (@RequestBody UserTransaction newUserTransaction) {
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
        Boolean success = user.transact(stock,newUserTransaction.amount,newUserTransaction.buy);
        if (success) {
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
