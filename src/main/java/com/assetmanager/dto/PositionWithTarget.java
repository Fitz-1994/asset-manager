package com.assetmanager.dto;

import java.time.Instant;

public class PositionWithTarget extends PositionResponse {
    private String target_code;
    private String target_name;
    private String target_market;
    private String target_currency;
    private Double last_price;
    private Double market_value;

    public String getTarget_code() { return target_code; }
    public void setTarget_code(String target_code) { this.target_code = target_code; }
    public String getTarget_name() { return target_name; }
    public void setTarget_name(String target_name) { this.target_name = target_name; }
    public String getTarget_market() { return target_market; }
    public void setTarget_market(String target_market) { this.target_market = target_market; }
    public String getTarget_currency() { return target_currency; }
    public void setTarget_currency(String target_currency) { this.target_currency = target_currency; }
    public Double getLast_price() { return last_price; }
    public void setLast_price(Double last_price) { this.last_price = last_price; }
    public Double getMarket_value() { return market_value; }
    public void setMarket_value(Double market_value) { this.market_value = market_value; }
}
