package com.CodeClan.PrinceJohn.components;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

@Component
public class SecurityBeans {

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    public Optional<RSAKey> loadRSA() {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            File publicKeyFile = new File("src/main/resources/rsa_public.key");
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile.toPath());
            File privateKeyFile = new File("src/main/resources/rsa_private.key");
            byte[] privateKeyBytes = Files.readAllBytes(privateKeyFile.toPath());
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            RSAPublicKey rsa_public = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            RSAPrivateKey rsa_private = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);
            RSAKey loaded = new RSAKey.Builder(rsa_public).privateKey(rsa_private).build();
            System.out.println("Loaded RSA Keys successfully");
            return Optional.ofNullable(loaded);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void checkRSA() {
        System.out.println("Loading RSA keys");
        try {
            loadRSA();
        } catch (Exception e) {
            System.out.println("Building new RSA keys");
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair pair = generator.generateKeyPair();
                PrivateKey privateKey = pair.getPrivate();
                PublicKey publicKey = pair.getPublic();
                try (FileOutputStream fos = new FileOutputStream("src/main/resources/rsa_public.key")) {
                    fos.write(publicKey.getEncoded());
                }
                try (FileOutputStream fos = new FileOutputStream("src/main/resources/rsa_private.key")) {
                    fos.write(privateKey.getEncoded());
                }
            } catch (Exception f) {
                throw new RuntimeException(f);
            }
        }
    }


}
