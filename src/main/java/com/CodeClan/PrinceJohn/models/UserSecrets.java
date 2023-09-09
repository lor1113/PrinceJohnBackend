package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.*;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "user_secrets")
public class UserSecrets {

    @Id
    public Long Id;
    public Boolean enabled2FA;
    public Boolean loginDisabled;
    public String secret2FA;
    @ElementCollection
    @MapKeyColumn(name = "device_id")
    @Column(name = "device_secret", length = 500)
    public Map<Long, String> transactionSecrets;
    public String passwordHash;
    public String lastTOTP;
    public Long securityId;
    @Column(unique = true)
    public String email;

    public UserSecrets(Long Id, String email) {
        this.Id = Id;
        this.email = email;
        this.enabled2FA = Boolean.FALSE;
        this.securityId = new SecureRandom().nextLong();
        this.loginDisabled = Boolean.FALSE;
        this.transactionSecrets = new HashMap<>();
    }

    public UserSecrets() {
    }

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

    public Map<Long, String> getTransactionSecrets() {
        return transactionSecrets;
    }

    public void setTransactionSecrets(Map<Long, String> transactionSecrets) {
        this.transactionSecrets = transactionSecrets;
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

    public Boolean getLoginDisabled() {
        return loginDisabled;
    }

    public void setLoginDisabled(Boolean disabled) {
        this.loginDisabled = disabled;
    }

    public String getLastTOTP() {
        return lastTOTP;
    }

    public void setLastTOTP(String lastTOTP) {
        this.lastTOTP = lastTOTP;
    }

    public Long getSecurityId() {
        return securityId;
    }

    public void setSecurityId(Long securityId) {
        this.securityId = securityId;
    }
}
