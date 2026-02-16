package com.assetmanager.dto;

import java.time.Instant;

public class ManualBalanceResponse {
    private Long id;
    private Long account_id;
    private Double amount;
    private String currency;
    private Instant recorded_at;
    private Instant created_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccount_id() { return account_id; }
    public void setAccount_id(Long account_id) { this.account_id = account_id; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getRecorded_at() { return recorded_at; }
    public void setRecorded_at(Instant recorded_at) { this.recorded_at = recorded_at; }
    public Instant getCreated_at() { return created_at; }
    public void setCreated_at(Instant created_at) { this.created_at = created_at; }
}
