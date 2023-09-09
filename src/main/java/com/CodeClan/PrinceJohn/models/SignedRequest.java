package com.CodeClan.PrinceJohn.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SignedRequest {
    @NotBlank
    public String signedPayload;
    @NotNull
    public long deviceID;
}
