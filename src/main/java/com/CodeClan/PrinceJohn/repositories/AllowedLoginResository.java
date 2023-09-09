package com.CodeClan.PrinceJohn.repositories;

import com.CodeClan.PrinceJohn.models.AllowedLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllowedLoginResository extends JpaRepository<AllowedLogin, Integer> {
    Optional<AllowedLogin> findByCode(String code);

    Optional<AllowedLogin> findByloginDeviceID(long loginDeviceID);
}
