package com.CodeClan.PrinceJohn.models;

import jakarta.persistence.Embeddable;

@Embeddable
public class StockBaseData {
    public String ticker;
    public String fullName;
    public String description;
}
