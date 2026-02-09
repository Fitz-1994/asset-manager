package com.assetmanager.entity;

import java.time.Instant;

public class AssetSnapshot {
    private Long id;
    private Long userId;
    private Instant snapshotAt;
    private String triggerType;
    private Double totalValueCny;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Instant getSnapshotAt() { return snapshotAt; }
    public void setSnapshotAt(Instant snapshotAt) { this.snapshotAt = snapshotAt; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public Double getTotalValueCny() { return totalValueCny; }
    public void setTotalValueCny(Double totalValueCny) { this.totalValueCny = totalValueCny; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
