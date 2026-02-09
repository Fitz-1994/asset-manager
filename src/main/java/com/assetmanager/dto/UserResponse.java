package com.assetmanager.dto;

import java.time.Instant;

public class UserResponse {
    private Long id;
    private String username;
    private Instant createdAt;

    public static UserResponse from(Long id, String username, Instant createdAt) {
        UserResponse r = new UserResponse();
        r.setId(id);
        r.setUsername(username);
        r.setCreatedAt(createdAt);
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
