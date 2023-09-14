package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.models.AllowedLogin;
import com.CodeClan.PrinceJohn.models.ProspectiveUser;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.AllowedLoginResository;
import com.CodeClan.PrinceJohn.repositories.ProspectiveUserRepository;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint")
public class AppEndpointControllerNS {

    @Autowired
    ProspectiveUserRepository prospectiveUserRepository;

    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AllowedLoginResository allowedLoginResository;

    @GetMapping("/userConfirm/{code}")
    public ResponseEntity<String> userConfirm(@Size(min = 2) @PathVariable String code) {
        Optional<ProspectiveUser> userToConfirm = prospectiveUserRepository.findById(code);
        if (userToConfirm.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ProspectiveUser userToMake = userToConfirm.get();
        LocalDateTime now = LocalDateTime.now();
        long timePassed = userToMake.hashDate.until(now, ChronoUnit.SECONDS);
        if (timePassed > 1800) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            User newUser = new User(userToMake);
            prospectiveUserRepository.delete(userToMake);
            userRepository.save(newUser);
            UserSecrets userSecrets = new UserSecrets(newUser.getId(), newUser.email);
            userSecrets.passwordHash = userToMake.passwordHash;
            userSecretsRepository.save(userSecrets);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        System.out.println("user confirmation success");
        return new ResponseEntity<>("Email confirmed. Your account has been made.",HttpStatus.OK);
    }
    @GetMapping("/userLoginConfirm/{code}")
    public ResponseEntity<String> userLoginConfirm(@Size(min = 2) @PathVariable String code) {
        Optional<AllowedLogin> optionalAllowedLogin = allowedLoginResository.findByCode(code);
        if (optionalAllowedLogin.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        AllowedLogin allowedLogin = optionalAllowedLogin.get();
        allowedLogin.state = Boolean.TRUE;
        allowedLoginResository.save(allowedLogin);
        System.out.println("login request confirmation success");
        return new ResponseEntity<>("Login request confirmed. You can log in now.", HttpStatus.OK);
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
