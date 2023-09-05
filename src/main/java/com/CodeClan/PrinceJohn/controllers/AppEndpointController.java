package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.EmailService;
import com.CodeClan.PrinceJohn.components.JwtTokenService;
import com.CodeClan.PrinceJohn.components.UserService;
import com.CodeClan.PrinceJohn.models.*;
import com.CodeClan.PrinceJohn.repositories.ProspectiveUserRepository;
import com.CodeClan.PrinceJohn.repositories.StockRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    ProspectiveUserRepository prospectiveUserRepository;

    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    EmailService emailService;


    @GetMapping("/stockData")
    public ResponseEntity<List<Stock>> getStocks() {
        return new ResponseEntity<>(stockRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/userData/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
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
    @PostMapping("/login")
    public ResponseEntity<String> login (@RequestBody LoginRequest loginRequest) {
        Optional<UserSecrets> optionalUserSecrets = userSecretsRepository.findByEmail(loginRequest.email);
        if (optionalUserSecrets.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserSecrets userSecrets = optionalUserSecrets.get();
        Boolean passwordMatch = passwordEncoder.matches(loginRequest.password, userSecrets.passwordHash);
        if (!passwordMatch) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (userSecrets.enabled2FA) {
            TimeProvider timeProvider = new SystemTimeProvider();
            CodeGenerator codeGenerator = new DefaultCodeGenerator();
            CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            Boolean match2FA = verifier.isValidCode(userSecrets.secret2FA,loginRequest.code2FA);
            if (!match2FA) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }
        Optional<String> accessToken = jwtTokenService.generateAccessToken(userSecrets, Boolean.TRUE);
        if (accessToken.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String jwtToken = accessToken.get();
        return new ResponseEntity<>(jwtToken,HttpStatus.OK);
    }

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
        Boolean success = user.transact(stock, newUserTransaction.amount, newUserTransaction.buy);
        if (success) {
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/userSignup")
    public ResponseEntity<?> userSignup(@RequestBody NewUser newUser) {
        if (userRepository.existsUserByEmail(newUser.email)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (prospectiveUserRepository.existsProspectiveUserByEmail(newUser.email)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        ProspectiveUser hashed = new ProspectiveUser(newUser);
        hashed.passwordHash = passwordEncoder.encode(newUser.password);
        try {
            prospectiveUserRepository.save(hashed);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String baseConfirm = "https://localhost:8080/appEndpoint/userConfirm/";
        emailService.sendEmail(hashed.email,baseConfirm + hashed.Id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/userConfirm/{id}")
    public ResponseEntity<?> userConfirm(@PathVariable String Id) {
        Optional<ProspectiveUser> userToConfirm = prospectiveUserRepository.findById(Id);
        if (userToConfirm.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProspectiveUser userToMake = userToConfirm.get();
        LocalDateTime now = LocalDateTime.now();
        long timePassed = userToMake.hashDate.until(now, ChronoUnit.SECONDS);
        if (timePassed > 1800) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        User newUser = new User(userToMake);
        prospectiveUserRepository.delete(userToMake);
        try {
            UserSecrets userSecrets = new UserSecrets(newUser.getId(),newUser.email);
            userSecrets.passwordHash = userToMake.passwordHash;
            userRepository.save(newUser);
            userSecretsRepository.save(userSecrets);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/userReConfirm")
    public ResponseEntity<?> userReConfirm(@RequestBody String email) {
        Optional<ProspectiveUser> userToConfirm = prospectiveUserRepository.findByEmail(email);
        if (userToConfirm.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProspectiveUser userToRefresh = userToConfirm.get();
        userToRefresh.newHash();
        prospectiveUserRepository.save(userToRefresh);
        String baseConfirm = "https://localhost:8080/appEndpoint/userConfirm/";
        emailService.sendEmail(userToRefresh.email,baseConfirm + userToRefresh.Id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/userSecurity")
    public ResponseEntity<?> userSecurity(@RequestBody String action) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}