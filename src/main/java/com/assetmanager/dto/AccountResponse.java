package com.assetmanager.dto;

import java.time.Instant;

public class AccountResponse {
    private Long id;
    private Long user_id;
    private String name;
    private String currency;
    private String currencyName;
    private String type;
    private String typeName;
    private String subtype;
    private Instant created_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }
    public Instant getCreated_at() { return created_at; }
    public void setCreated_at(Instant created_at) { this.created_at = created_at; }
}
