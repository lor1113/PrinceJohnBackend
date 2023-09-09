package com.CodeClan.PrinceJohn.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class NewUser {
    @Email
    public String email;
    @NotNull
    public LocalDate birthday;
    @NotBlank
    public String username;
    @NotBlank
    public String password;
    @Min(1)
    public long loginDeviceID;
}
