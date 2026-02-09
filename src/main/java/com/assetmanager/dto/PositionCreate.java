package com.assetmanager.dto;

public class PositionCreate {
    private Long target_id;
    private Double quantity;

    public Long getTarget_id() { return target_id; }
    public void setTarget_id(Long target_id) { this.target_id = target_id; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
