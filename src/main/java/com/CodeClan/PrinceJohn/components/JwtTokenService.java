package com.CodeClan.PrinceJohn.components;

import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;


@Component
public class JwtTokenService {

    @Autowired SecurityBeans securityBeans;
    private static final long EXPIRE_DURATION_ACCESS = 4 * 60 * 60 * 1000; // 4 hours.
    private static final long EXPIRE_DURATION_REFRESH = 10 * 24 * 60 * 60 * 1000; // 10 days.

    private RSAKey rsaKey;

    @PostConstruct
    private void getRSA () {
        securityBeans.checkRSA();
        RSAKey rsaLoaded = securityBeans.loadRSA().orElseThrow();
        this.rsaKey = rsaLoaded;
        System.out.println("Loaded RSA keys for JWT");
    }

    public Optional<String> generateAccessToken(UserSecrets userSecrets, Boolean refresh) {
        long expiration;
        if (refresh) {
            expiration = EXPIRE_DURATION_REFRESH;
        } else {
            expiration= EXPIRE_DURATION_ACCESS;
        }
        Date now = new Date();
        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                .claim("user_id",userSecrets.Id)
                .claim("user_email", userSecrets.email)
                .claim("security_id",userSecrets.getSecurityId())
                .claim("refresh",refresh)
                .issuer("PrinceJohn")
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(new Date(now.getTime() + expiration))
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWEHeader header = new JWEHeader(
                JWEAlgorithm.RSA_OAEP_256,
                EncryptionMethod.A128GCM
        );
        try {
            EncryptedJWT jwt = new EncryptedJWT(header, jwtClaims);
            RSAEncrypter encrypter = new RSAEncrypter(rsaKey.toRSAPublicKey());
            jwt.encrypt(encrypter);
            Optional<String > jwtString = Optional.of(jwt.serialize());
            return jwtString;
        } catch (Exception e) {
            return Optional.empty();
        }

    }
    public Optional<JWTClaimsSet> decryptToken(String token) {
        try {
            EncryptedJWT jwt = EncryptedJWT.parse(token);
            RSADecrypter decrypter = new RSADecrypter(rsaKey.toPrivateKey());
            jwt.decrypt(decrypter);
            return Optional.of(jwt.getJWTClaimsSet());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}