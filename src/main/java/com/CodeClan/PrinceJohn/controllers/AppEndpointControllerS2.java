package com.CodeClan.PrinceJohn.controllers;

import com.CodeClan.PrinceJohn.components.JwtTokenService;
import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/appEndpoint/s2")
public class AppEndpointControllerS2 {

    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    JwtTokenService jwtTokenService;

    @PostMapping("/accessToken")
    public ResponseEntity<String> accessToken(Principal principal) {
        Long id = Long.valueOf(principal.getName());
        Optional<UserSecrets> secretsOut = userSecretsRepository.findById(id);
        if (secretsOut.isPresent()) {
            try {
                UserSecrets userSecrets = secretsOut.get();
                String newToken = jwtTokenService.generateAccessToken(userSecrets, Boolean.FALSE).orElseThrow();
                System.out.println("access token success");
                return new ResponseEntity<>(newToken, HttpStatus.OK);
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
