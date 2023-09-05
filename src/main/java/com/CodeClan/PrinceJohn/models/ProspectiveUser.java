package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prospective_users")
public class ProspectiveUser {
    public String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    @Column(unique=true)
    public String email;
    public LocalDate birthday;

    public LocalDateTime getHashDate() {
        return hashDate;
    }

    public void setHashDate(LocalDateTime hashDate) {
        this.hashDate = hashDate;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public LocalDateTime hashDate;

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String passwordHash;

    public Boolean enable2FA;

    @Id
    public String Id;

    public ProspectiveUser(NewUser newUser) {
        this.username = newUser.username;
        this.email = newUser.email;
        this.birthday = newUser.birthday;
        newHash();
    }

    public void newHash () {
        SecureRandom rand = new SecureRandom();
        String hash = String.valueOf(this.hashCode());
        String randomSalt = String.valueOf(rand.nextInt(1000000000));
        this.hashDate = LocalDateTime.now();
        this.Id = hash + randomSalt;
    }

    public ProspectiveUser () {}
}
