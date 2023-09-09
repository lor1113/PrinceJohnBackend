package com.CodeClan.PrinceJohn.components;

import com.CodeClan.PrinceJohn.models.SignedRequest;
import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequestVerifier {
    @Autowired
    UserSecretsRepository userSecretsRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    public Object verifySignature(long userID, SignedRequest signedRequest, Class<?> target) {
        UserSecrets userSecrets = userSecretsRepository.findById(userID).orElseThrow();
        return verifySignature(userSecrets, signedRequest, target);
    }

    public Object verifySignature(UserSecrets userSecrets, SignedRequest signedRequest, Class<?> target) {
        System.out.println("Starting request verifier");
        JWSObject jwsObject;
        Boolean success;
        try {
            String keys = userSecrets.transactionSecrets.get(signedRequest.deviceID);
            JWK key = JWK.parse(keys);
            System.out.println(key);
            RSASSAVerifier verifier = new RSASSAVerifier(key.toRSAKey());
            jwsObject = JWSObject.parse(signedRequest.signedPayload);
            System.out.println(jwsObject);
            success = jwsObject.verify(verifier);
            System.out.println(success);
        } catch (Exception e) {
            System.out.println("Error in JWS parsing");
            return Boolean.FALSE;
        }
        if (success) {
            String payload = jwsObject.getPayload().toString();
            System.out.println(payload);
            try {
                Object output = objectMapper.readValue(payload, target);
                System.out.println("Finished request verifier");
                return output;
            } catch (Exception e) {
                System.out.println("Failed JSON object parsing");
                return Boolean.FALSE;
            }
        }
        System.out.println("Failed JWS verification");
        return Boolean.FALSE;
    }
}
