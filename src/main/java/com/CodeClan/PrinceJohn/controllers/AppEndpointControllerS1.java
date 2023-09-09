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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint/s1")
public class AppEndpointControllerS1 {
    @Autowired
    StockRepository stockRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;


    @GetMapping("/stockData")
    public ResponseEntity<List<Stock>> getStocks() {
        return new ResponseEntity<>(stockRepository.findAll(), HttpStatus.OK);
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
