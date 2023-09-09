package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.RequestVerifier;
import com.CodeClan.PrinceJohn.models.*;
import com.CodeClan.PrinceJohn.repositories.StockRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint/s3")
public class AppEndpointControllerS3 {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    RequestVerifier requestVerifier;

    @PostMapping("/userStockTransaction")
    public ResponseEntity<?> userStockTransact(@RequestHeader("X-Operation-Id") Long sendID,
                                               @Valid @RequestBody SignedRequest signedRequest,
                                               Principal principal) {
        Long id = Long.valueOf(principal.getName());
        Object output = requestVerifier.verifySignature(id, signedRequest, UserStockTransaction.class);
        if (output == Boolean.FALSE) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        @Valid UserStockTransaction newStockTransaction = (UserStockTransaction) output;
        if (!id.equals(newStockTransaction.user_id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!(sendID == newStockTransaction.operation_id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<User> userOut = userRepository.findById(id);
        if (userOut.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Map<Stock, Integer> fullOrder = new HashMap<>();
        for (Map.Entry<String, Integer> entry : newStockTransaction.order.entrySet()) {
            String stockTicker = entry.getKey();
            Integer quantity = entry.getValue();
            Optional<Stock> stockOut = stockRepository.findByTicker(stockTicker);
            if (stockOut.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Stock stock = stockOut.get();
            fullOrder.put(stock, quantity);
        }
        User user = userOut.get();
        Boolean success;
        if (newStockTransaction.buy) {
            success = user.buyStock(fullOrder);
        } else {
            success = user.sellStock(fullOrder);
        }
        if (success) {
            user.lastTransaction = sendID;
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/userBankTransaction")
    public ResponseEntity<?> userBankTransact(@RequestHeader("X-Operation-Id") Long sendID,
                                              @Valid @RequestBody SignedRequest signedRequest,
                                              Principal principal) {
        Long id = Long.valueOf(principal.getName());
        Object output = requestVerifier.verifySignature(id, signedRequest, UserBankTransaction.class);
        if (output == Boolean.FALSE) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        @Valid UserBankTransaction newBankTransaction = (UserBankTransaction) output;
        if (!id.equals(newBankTransaction.user_id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!(sendID == newBankTransaction.operation_id)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Optional<User> userOut = userRepository.findById(newBankTransaction.user_id);
        if (userOut.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = userOut.get();
        Boolean success;
        if (newBankTransaction.withdrawal) {
            success = user.userWithdraw(newBankTransaction.quantity);
        } else {
            success = user.userDeposit(newBankTransaction.quantity);
        }
        if (success) {
            user.lastTransaction = sendID;
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Long> testFunction(Principal principal) {
        Long id = Long.valueOf(principal.getName());
        return new ResponseEntity<>(id, HttpStatus.OK);
    }
}
