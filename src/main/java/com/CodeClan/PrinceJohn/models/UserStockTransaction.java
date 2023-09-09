package com.CodeClan.PrinceJohn.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class UserStockTransaction {
    @NotNull
    public Long user_id;
    public Map<@NotNull String, @Min(1) Integer> order;
    public Boolean buy;
    @NotNull
    public long operation_id;
}
