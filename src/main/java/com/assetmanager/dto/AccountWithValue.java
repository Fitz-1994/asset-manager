package com.assetmanager.dto;

public class AccountWithValue extends AccountResponse {
    private double value;

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
