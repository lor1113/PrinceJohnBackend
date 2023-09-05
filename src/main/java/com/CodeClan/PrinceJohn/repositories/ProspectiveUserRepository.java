package com.CodeClan.PrinceJohn.repositories;

import com.CodeClan.PrinceJohn.models.ProspectiveUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProspectiveUserRepository extends JpaRepository<ProspectiveUser, String> {
    Boolean existsProspectiveUserByEmail (String email);
    Optional<ProspectiveUser> findByEmail(String email);
}
