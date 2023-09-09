package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "allowed_logins")
public class AllowedLogin {

    public String email;
    @Id
    public String code;
    public LocalDateTime expiry;
    public Boolean state;
    public long loginDeviceID;

    public AllowedLogin() {
    }

    public AllowedLogin(long loginDeviceID, long newCode, String email) {
        this.loginDeviceID = loginDeviceID;
        this.email = email;
        this.code = String.valueOf(newCode);
        this.state = Boolean.FALSE;
        this.expiry = LocalDateTime.now().plusMinutes(20);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public long getLoginDeviceID() {
        return loginDeviceID;
    }

    public void setLoginDeviceID(long loginDeviceID) {
        this.loginDeviceID = loginDeviceID;
    }

}
