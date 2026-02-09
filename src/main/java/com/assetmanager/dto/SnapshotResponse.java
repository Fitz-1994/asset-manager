package com.assetmanager.dto;

import java.time.Instant;

public class SnapshotResponse {
    private Long id;
    private Long user_id;
    private Instant snapshot_at;
    private String trigger_type;
    private Double total_value_cny;
    private Instant created_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }
    public Instant getSnapshot_at() { return snapshot_at; }
    public void setSnapshot_at(Instant snapshot_at) { this.snapshot_at = snapshot_at; }
    public String getTrigger_type() { return trigger_type; }
    public void setTrigger_type(String trigger_type) { this.trigger_type = trigger_type; }
    public Double getTotal_value_cny() { return total_value_cny; }
    public void setTotal_value_cny(Double total_value_cny) { this.total_value_cny = total_value_cny; }
    public Instant getCreated_at() { return created_at; }
    public void setCreated_at(Instant created_at) { this.created_at = created_at; }
}
