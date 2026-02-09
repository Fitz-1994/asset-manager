package com.assetmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreate {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 64)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 1)
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username != null ? username.trim() : null; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
