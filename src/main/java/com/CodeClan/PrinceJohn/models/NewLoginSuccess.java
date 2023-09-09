package com.CodeClan.PrinceJohn.models;

public class NewLoginSuccess {
    public String token;
    public long device_id;

    public NewLoginSuccess(String token, long device_id) {
        this.token = token;
        this.device_id = device_id;
    }
}
