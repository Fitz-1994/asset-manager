package com.assetmanager.dto;

public class PositionCreate {
    private Long target_id;
    private Double quantity;
    
    // For creating new target inline
    private String target_market;
    private String target_code;
    private String target_name;
    private String target_currency;

    public Long getTarget_id() { return target_id; }
    public void setTarget_id(Long target_id) { this.target_id = target_id; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getTarget_market() { return target_market; }
    public void setTarget_market(String target_market) { this.target_market = target_market; }
    public String getTarget_code() { return target_code; }
    public void setTarget_code(String target_code) { this.target_code = target_code; }
    public String getTarget_name() { return target_name; }
    public void setTarget_name(String target_name) { this.target_name = target_name; }
    public String getTarget_currency() { return target_currency; }
    public void setTarget_currency(String target_currency) { this.target_currency = target_currency; }
}
