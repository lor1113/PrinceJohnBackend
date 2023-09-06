package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.JwtTokenService;
import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint/s4")
public class AppEndpointControllerS4 {
    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public ResponseEntity<String> login (Principal principal) {
        Long id = Long.valueOf(principal.getName());
        Optional<UserSecrets> optionalUserSecrets = userSecretsRepository.findById(id);
        if (optionalUserSecrets.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserSecrets userSecrets = optionalUserSecrets.get();
        Optional<String> accessToken = jwtTokenService.generateAccessToken(userSecrets, Boolean.TRUE);
        if (accessToken.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String jwtToken = accessToken.get();
        return new ResponseEntity<>(jwtToken,HttpStatus.OK);
    }
}
