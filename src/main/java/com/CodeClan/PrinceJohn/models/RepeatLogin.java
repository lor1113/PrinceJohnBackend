package com.CodeClan.PrinceJohn.models;

import jakarta.validation.constraints.NotNull;

public class RepeatLogin {
    @NotNull
    public long operation_id;
    @NotNull
    public long device_id;
}
