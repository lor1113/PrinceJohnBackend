package com.CodeClan.PrinceJohn.repositories;

import com.CodeClan.PrinceJohn.models.UserSecrets;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSecretsRepository extends JpaRepository<UserSecrets, Long> {
    Optional<UserSecrets> findByEmail(String email);
}
