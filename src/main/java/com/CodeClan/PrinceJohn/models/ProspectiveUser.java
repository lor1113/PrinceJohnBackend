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
    @Column(unique = true)
    public String email;
    public LocalDate birthday;
    public LocalDateTime hashDate;
    public String passwordHash;
    @Id
    public String Id;

    public ProspectiveUser(NewUser newUser) {
        this.username = newUser.username;
        this.email = newUser.email.toLowerCase();
        this.birthday = newUser.birthday;
        newHash();
    }

    public ProspectiveUser() {
    }

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void newHash() {
        SecureRandom rand = new SecureRandom();
        String hash = String.valueOf(this.hashCode());
        String randomSalt = String.valueOf(rand.nextLong());
        this.hashDate = LocalDateTime.now();
        this.Id = hash + randomSalt;
    }
}
