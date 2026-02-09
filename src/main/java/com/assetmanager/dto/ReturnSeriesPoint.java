package com.assetmanager.dto;

import java.time.Instant;

public class ReturnSeriesPoint {
    private String date;
    private Instant timestamp;
    private double value;
    private Double return_pct;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public Double getReturn_pct() { return return_pct; }
    public void setReturn_pct(Double return_pct) { this.return_pct = return_pct; }
}
