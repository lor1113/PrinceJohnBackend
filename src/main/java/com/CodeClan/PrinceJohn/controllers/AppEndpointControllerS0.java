package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.EmailService;
import com.CodeClan.PrinceJohn.models.NewUser;
import com.CodeClan.PrinceJohn.models.ProspectiveUser;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.ProspectiveUserRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
}