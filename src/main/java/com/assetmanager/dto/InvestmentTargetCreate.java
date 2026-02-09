package com.assetmanager.dto;

public class InvestmentTargetCreate {
    private String market;
    private String code;
    private String name;
    private String currency;

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
