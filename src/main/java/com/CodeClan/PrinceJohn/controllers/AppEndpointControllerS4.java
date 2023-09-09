package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.EmailService;
import com.CodeClan.PrinceJohn.components.JwtTokenService;
import com.CodeClan.PrinceJohn.components.RequestVerifier;
import com.CodeClan.PrinceJohn.models.*;
import com.CodeClan.PrinceJohn.repositories.AllowedLoginResository;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint/s4")
public class AppEndpointControllerS4 {
    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    RequestVerifier requestVerifier;

    @Autowired
    AllowedLoginResository allowedLoginResository;

    @Autowired
    EmailService emailService;

    @PostMapping("/repeatLogin")
    public ResponseEntity<String> login(@RequestBody @Valid SignedRequest signedRequest, Principal principal,
                                        @RequestHeader("X-Operation-Id") long sendID) {
        Long id = Long.valueOf(principal.getName());
        Optional<UserSecrets> optionalUserSecrets = userSecretsRepository.findById(id);
        if (optionalUserSecrets.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserSecrets userSecrets = optionalUserSecrets.get();
        Object output = requestVerifier.verifySignature(userSecrets, signedRequest, RepeatLogin.class);
        if (output == Boolean.FALSE) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        @Valid RepeatLogin repeatLogin = (RepeatLogin) output;
        if (repeatLogin.device_id != signedRequest.deviceID) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (repeatLogin.operation_id != sendID) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<String> accessToken = jwtTokenService.generateAccessToken(userSecrets, Boolean.TRUE);
        if (accessToken.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String jwtToken = accessToken.get();
        return new ResponseEntity<>(jwtToken, HttpStatus.OK);
    }

    @PostMapping("/requestNewLogin")
    public ResponseEntity<String> newLoginRequest(@Valid @RequestBody NewLoginRequest request, Principal principal) {
        SecureRandom rand = new SecureRandom();
        Long newCode = (rand.nextLong() * -1) + 10;
        String baseURL = "https://localhost:8080/appEndpoint/userLoginConfirm/";
        AllowedLogin login = new AllowedLogin(request.loginDeviceID, newCode, request.email);
        allowedLoginResository.save(login);
        emailService.sendEmail(request.email, baseURL + newCode);
        return new ResponseEntity<>(baseURL + newCode, HttpStatus.OK);
    }

    @PostMapping("/newLogin")
    public ResponseEntity<NewLoginSuccess> newLogin(@RequestHeader("X-Operation-Id") Long sendID,
                                                    @Valid @RequestBody NewLogin newLogin, Principal principal) {
        System.out.println(newLogin.operation_id);
        System.out.println(sendID);
        if (!(sendID == newLogin.operation_id)) {
            System.out.println("operation ID mismatch");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Long id = Long.valueOf(principal.getName());
        Optional<UserSecrets> optionalUserSecrets = userSecretsRepository.findById(id);
        if (optionalUserSecrets.isEmpty()) {
            System.out.println("failed finding user secrets");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserSecrets userSecrets = optionalUserSecrets.get();
        Optional<AllowedLogin> optionalAllowedLogin = allowedLoginResository.findByloginDeviceID(newLogin.loginDeviceID);
        if (optionalAllowedLogin.isEmpty()) {
            System.out.println("failed finding allowed login");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        AllowedLogin allowedLogin = optionalAllowedLogin.get();
        LocalDateTime now = LocalDateTime.now();
        if (!allowedLogin.state) {
            System.out.println("failed allowed login state");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (allowedLogin.expiry.isBefore(now)) {
            System.out.println("failed allowed login expiry");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (!(allowedLogin.loginDeviceID == newLogin.loginDeviceID)) {
            System.out.println("failed device id");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (!(Objects.equals(allowedLogin.email, newLogin.email))) {
            System.out.println("failed email");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Optional<String> accessToken = jwtTokenService.generateAccessToken(userSecrets, Boolean.TRUE);
        if (accessToken.isEmpty()) {
            System.out.println("failed tokengen");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        SecureRandom rand = new SecureRandom();
        Long deviceID = (rand.nextLong() * -1) + 10;
        while (userSecrets.transactionSecrets.containsKey(deviceID)) {
            deviceID = (rand.nextLong() * -1) + 10;
        }
        String jwtToken = accessToken.get();
        allowedLoginResository.delete(allowedLogin);
        userSecrets.transactionSecrets.put(deviceID, newLogin.secret);
        userSecretsRepository.save(userSecrets);
        NewLoginSuccess newLoginSuccess = new NewLoginSuccess(jwtToken, deviceID);
        return new ResponseEntity<>(newLoginSuccess, HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<Long> testFunction(Principal principal) {
        Long id = Long.valueOf(principal.getName());
        return new ResponseEntity<>(id, HttpStatus.OK);
    }
}
