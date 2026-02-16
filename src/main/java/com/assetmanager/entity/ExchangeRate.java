package com.assetmanager.entity;

import java.time.Instant;

public class ExchangeRate {
    private Long id;
    private String fromCurrency;
    private String toCurrency;
    private Double rate;
    private Instant recordedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }
    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; }
    public Instant getRecordedAt() { return recordedAt; }
    public void setRecordedAt(Instant recordedAt) { this.recordedAt = recordedAt; }
}
