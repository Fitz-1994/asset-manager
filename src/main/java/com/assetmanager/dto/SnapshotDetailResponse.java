package com.assetmanager.dto;

public class SnapshotDetailResponse {
    private Long id;
    private Long snapshot_id;
    private Long account_id;
    private Double value;
    private String currency;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSnapshot_id() { return snapshot_id; }
    public void setSnapshot_id(Long snapshot_id) { this.snapshot_id = snapshot_id; }
    public Long getAccount_id() { return account_id; }
    public void setAccount_id(Long account_id) { this.account_id = account_id; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
