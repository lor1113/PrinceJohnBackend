package com.CodeClan.PrinceJohn.tools;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import java.security.interfaces.RSAPrivateKey;
import java.text.ParseException;

public class generateTransactionSecrets {
    public static void main(String[] args) throws JOSEException, ParseException {
        RSAKey rsaKey = new RSAKeyGenerator(2048).generate();
        RSAPrivateKey privateKey = rsaKey.toRSAPrivateKey();
        JWK jwktest = rsaKey.toPublicJWK();
        System.out.println(jwktest.toJSONString());
        JWK jwktest1 = JWK.parse(jwktest.toJSONString());
        System.out.println(jwktest1);
    }
}
