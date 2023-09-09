package com.CodeClan.PrinceJohn.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class NewLoginRequest {
    @Email
    public String email;
    @NotNull
    public long loginDeviceID;
}
