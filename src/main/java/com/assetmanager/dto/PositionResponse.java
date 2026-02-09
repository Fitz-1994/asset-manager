package com.assetmanager.dto;

import java.time.Instant;

public class PositionResponse {
    private Long id;
    private Long account_id;
    private Long target_id;
    private Double quantity;
    private Instant updated_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccount_id() { return account_id; }
    public void setAccount_id(Long account_id) { this.account_id = account_id; }
    public Long getTarget_id() { return target_id; }
    public void setTarget_id(Long target_id) { this.target_id = target_id; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Instant getUpdated_at() { return updated_at; }
    public void setUpdated_at(Instant updated_at) { this.updated_at = updated_at; }
}
