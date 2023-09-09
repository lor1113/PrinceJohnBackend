package com.CodeClan.PrinceJohn;

import com.CodeClan.PrinceJohn.components.StockService;
import com.CodeClan.PrinceJohn.models.User;
import com.CodeClan.PrinceJohn.models.UserSecrets;
import com.CodeClan.PrinceJohn.repositories.UserRepository;
import com.CodeClan.PrinceJohn.repositories.UserSecretsRepository;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PrinceJohnDBSetup {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserSecretsRepository userSecretsRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    StockService stockService;

    @BeforeAll
    public void dbClear() {
        userRepository.deleteAll();
        userSecretsRepository.deleteAll();
        stockService.loadStockData();
    }

    @Test
    public void setupUserOne() throws IOException {
        User testUser = new User("Lorenzo", "Lorenzocurcio2@gmail.com", LocalDate.of(2001, 4, 7));
        userRepository.save(testUser);
        UserSecrets userSecrets = new UserSecrets(testUser.getId(), testUser.email);
        String content = Files.readString(Path.of("src/test/java/com/CodeClan/PrinceJohn/rsaPublic1.txt"));
        userSecrets.transactionSecrets.put(999L, content);
        userSecrets.passwordHash = passwordEncoder.encode("LorenzoPassword");
        userSecretsRepository.save(userSecrets);
    }

    @Test
    public void setupUserTwo() {
        User testUser = new User("Felix", "Felix22@gmail.com", LocalDate.of(2000, 11, 5));
        userRepository.save(testUser);
        UserSecrets userSecrets = new UserSecrets(testUser.getId(), testUser.email);
        userSecrets.passwordHash = passwordEncoder.encode("FelixPassword");
        userSecrets.enabled2FA = Boolean.TRUE;
        SecretGenerator secretGenerator = new DefaultSecretGenerator(64);
        userSecrets.secret2FA = secretGenerator.generate();
        userSecretsRepository.save(userSecrets);
    }

    @Test
    public void setupUserThree() {
        User testUser = new User("Disabled", "Disabled@gmail.com", LocalDate.of(2000, 1, 1));
        userRepository.save(testUser);
        UserSecrets userSecrets = new UserSecrets(testUser.getId(), testUser.email);
        userSecrets.loginDisabled = Boolean.TRUE;
        userSecrets.passwordHash = passwordEncoder.encode("DisabledPassword");
        userSecretsRepository.save(userSecrets);
    }
}
