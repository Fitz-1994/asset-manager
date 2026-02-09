package com.assetmanager.entity;

import java.time.Instant;

public class InvestmentTarget {
    private Long id;
    private String market;
    private String code;
    private String name;
    private String currency;
    private Double lastPrice;
    private Instant priceUpdatedAt;
    private Instant createdAt;

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
    public Double getLastPrice() { return lastPrice; }
    public void setLastPrice(Double lastPrice) { this.lastPrice = lastPrice; }
    public Instant getPriceUpdatedAt() { return priceUpdatedAt; }
    public void setPriceUpdatedAt(Instant priceUpdatedAt) { this.priceUpdatedAt = priceUpdatedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
