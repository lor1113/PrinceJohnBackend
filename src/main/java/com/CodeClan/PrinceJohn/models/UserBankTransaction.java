package com.CodeClan.PrinceJohn.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UserBankTransaction {

    @NotNull
    public Long user_id;
    @Min(1)
    public Integer quantity;
    public Boolean withdrawal;
    @NotNull
    public long operation_id;
}
