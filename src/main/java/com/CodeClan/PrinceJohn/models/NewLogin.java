package com.CodeClan.PrinceJohn.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NewLogin {
    @NotBlank
    public String secret;
    @NotNull
    public long loginDeviceID;

    @Email
    public String email;

    @NotNull
    public long operation_id;
}
