package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Random;

@Entity
@Table(name="user_secrets")
public class UserSecrets {

    @Id
    public Long Id;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getSecret2FA() {
        return secret2FA;
    }

    public void setSecret2FA(String secret2FA) {
        this.secret2FA = secret2FA;
    }

    public String getTransactionSecret() {
        return transactionSecret;
    }

    public void setTransactionSecret(String transactionSecret) {
        this.transactionSecret = transactionSecret;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getEnabled2FA() {
        return enabled2FA;
    }

    public void setEnabled2FA(Boolean enabled2FA) {
        this.enabled2FA = enabled2FA;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean enabled2FA;

    public String secret2FA;
    public String transactionSecret;
    public String passwordHash;

    public Long getSecurityId() {
        return securityId;
    }

    public void setSecurityId(Long securityId) {
        this.securityId = securityId;
    }

    public Long securityId;

    @Column(unique=true)
    public String email;

    public UserSecrets (Long Id, String email) {
        this.Id = Id;
        this.email = email;
        this.enabled2FA = Boolean.FALSE;
        this.securityId = new Random().nextLong();
    }

    public UserSecrets () {}
}
