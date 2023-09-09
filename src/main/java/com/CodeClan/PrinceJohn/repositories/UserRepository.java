package com.CodeClan.PrinceJohn.repositories;

import com.CodeClan.PrinceJohn.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsUserByEmail(String email);

    User findUserByEmail(String mail);

}
