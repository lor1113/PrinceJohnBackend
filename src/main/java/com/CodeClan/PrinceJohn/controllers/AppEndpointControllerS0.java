package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.EmailService;
import com.CodeClan.PrinceJohn.models.AllowedLogin;
import com.CodeClan.PrinceJohn.models.NewUser;
import com.CodeClan.PrinceJohn.models.ProspectiveUser;
import com.CodeClan.PrinceJohn.repositories.AllowedLoginResository;
import com.CodeClan.PrinceJohn.repositories.ProspectiveUserRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint/s0")
public class AppEndpointControllerS0 {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProspectiveUserRepository prospectiveUserRepository;

    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EmailService emailService;

    @Autowired
    AllowedLoginResository allowedLoginResository;


    @PostMapping("/userSignup")
    public ResponseEntity<?> userSignup(@Valid @RequestBody NewUser newUser) {
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
        AllowedLogin login = new AllowedLogin(newUser.loginDeviceID, 0L, newUser.email);
        login.state = Boolean.TRUE;
        allowedLoginResository.save(login);
        String baseConfirm = "https://localhost:8080/appEndpoint/userConfirm/";
        emailService.sendEmail(hashed.email, baseConfirm + hashed.Id);
        return new ResponseEntity<>(baseConfirm + hashed.Id, HttpStatus.OK);
    }

    @PostMapping("/userReConfirm")
    public ResponseEntity<?> userReConfirm(@Email @RequestBody String email) {
        Optional<ProspectiveUser> userToConfirm = prospectiveUserRepository.findByEmail(email);
        if (userToConfirm.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProspectiveUser userToRefresh = userToConfirm.get();
        userToRefresh.newHash();
        prospectiveUserRepository.save(userToRefresh);
        String baseConfirm = "https://localhost:8080/appEndpoint/userConfirm/";
        emailService.sendEmail(userToRefresh.email, baseConfirm + userToRefresh.Id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testFunction() {
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}