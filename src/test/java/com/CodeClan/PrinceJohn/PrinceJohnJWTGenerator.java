package com.CodeClan.PrinceJohn;

import com.CodeClan.PrinceJohn.components.JwtTokenService;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PrinceJohnJWTGenerator {
    private static final long expiration = 10 * 24 * 60 * 60 * 1000;
    @Autowired
    JwtTokenService jwtTokenService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserSecretsRepository userSecretsRepository;
    BufferedWriter writer;
    private String fileName = "jwt_tokens.txt";
    private Map<String, Object> baseClaims;
    private Date now;
    private long userID1;
    private long securityID1;
    private long userID2;
    private long securityID2;

    @BeforeAll
    public void initFile() throws IOException {
        writer = new BufferedWriter(new FileWriter(fileName, false));
        User user1 = userRepository.findUserByEmail("Lorenzocurcio2@gmail.com");
        UserSecrets userSecrets = userSecretsRepository.findByEmail("Lorenzocurcio2@gmail.com").orElseThrow();
        userID1 = user1.getId();
        securityID1 = userSecrets.securityId;

        User user2 = userRepository.findUserByEmail("Felix22@gmail.com");
        UserSecrets userSecrets2 = userSecretsRepository.findByEmail("Felix22@gmail.com").orElseThrow();
        userID2 = user2.getId();
        securityID2 = userSecrets2.securityId;
    }

    @AfterAll
    public void closeFile() throws IOException {
        writer.close();
    }

    @BeforeEach
    public void initTest() {
        now = new Date();
        baseClaims = new HashMap<>();
        baseClaims.put("iss", "PrinceJohn");
        baseClaims.put("user_id", userID1);
        baseClaims.put("user_email", "Lorenzocurcio2@gmail.com");
        baseClaims.put("security_id", securityID1);
        baseClaims.put("refresh", Boolean.FALSE);
        baseClaims.put("nbf", now);
        baseClaims.put("iat", now);
        baseClaims.put("exp", new Date(now.getTime() + expiration));
    }

    @Test
    void generateValidAccessJWT() throws IOException {
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("valid_jwt_access:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateValidAccessJWT2() throws IOException {
        baseClaims.put("user_id", userID2);
        baseClaims.put("user_email", "Felix22@gmail.com");
        baseClaims.put("security_id", securityID2);
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("valid_jwt_access_2:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateValidRefreshJWT() throws IOException {
        baseClaims.put("refresh", Boolean.TRUE);
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("valid_jwt_refresh:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateInvalidIssuerJWT() throws IOException {
        baseClaims.put("iss", "itsame");
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("invalid_issuer:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateInvalidUserIDJWT() throws IOException {
        baseClaims.put("user_id", 777777L);
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("invalid_user_id:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateInvalidUserEmailJWT() throws IOException {
        baseClaims.put("user_email", "999.lol");
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("invalid_user_email:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateInvalidSecurityIDJWT() throws IOException {
        baseClaims.put("security_id", 0L);
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("invalid_security_id:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateFutureNBFJWT() throws IOException {
        baseClaims.put("nbf", new Date(now.getTime() + expiration));
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("invalid_notbefore_time:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateFutureIssueJWT() throws IOException {
        baseClaims.put("iat", new Date(now.getTime() + expiration));
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("invalid_issue_time:");
        writer.write(token);
        writer.newLine();
    }

    @Test
    void generateExpiredJWT() throws IOException {
        baseClaims.put("exp", new Date(now.getTime() - expiration));
        System.out.println(new Date(now.getTime() - expiration));
        String token = jwtTokenService.generateCustomToken(baseClaims).orElseThrow();
        writer.write("invalid_expiry:");
        writer.write(token);
        writer.newLine();
    }
}
