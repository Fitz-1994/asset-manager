package com.assetmanager.dto;

import java.time.Instant;

public class ManualBalanceCreate {
    private Double amount;
    private Instant recorded_at;

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Instant getRecorded_at() { return recorded_at; }
    public void setRecorded_at(Instant recorded_at) { this.recorded_at = recorded_at; }
}
