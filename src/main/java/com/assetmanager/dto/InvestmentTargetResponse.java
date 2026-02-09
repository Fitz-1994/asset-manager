package com.assetmanager.dto;

import java.time.Instant;

public class InvestmentTargetResponse {
    private Long id;
    private String market;
    private String code;
    private String name;
    private String currency;
    private Double last_price;
    private Instant price_updated_at;
    private Instant created_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Double getLast_price() { return last_price; }
    public void setLast_price(Double last_price) { this.last_price = last_price; }
    public Instant getPrice_updated_at() { return price_updated_at; }
    public void setPrice_updated_at(Instant price_updated_at) { this.price_updated_at = price_updated_at; }
    public Instant getCreated_at() { return created_at; }
    public void setCreated_at(Instant created_at) { this.created_at = created_at; }
}
