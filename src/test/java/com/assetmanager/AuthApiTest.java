package com.assetmanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("认证接口测试")
class AuthApiTest extends BaseApiTest {

    @Test
    @DisplayName("注册成功并返回 token 与用户信息")
    void register_returnsTokenAndUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"testuser1\",\"password\":\"pass123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isNotEmpty())
            .andExpect(jsonPath("$.token_type").value("bearer"))
            .andExpect(jsonPath("$.user.username").value("testuser1"))
            .andExpect(jsonPath("$.user.id").isNumber());
    }

    @Test
    @DisplayName("重复用户名注册返回 400 且 body 含 detail")
    void register_duplicateUsername_returns400() throws Exception {
        String uname = "dupuser-" + System.currentTimeMillis();
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"" + uname + "\",\"password\":\"pass\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"" + uname + "\",\"password\":\"other\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Username already registered"));
    }

    @Test
    @DisplayName("空用户名注册返回 422 且 body 含 detail")
    void register_blankUsername_returns422WithDetail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"  \",\"password\":\"pass\"}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    @DisplayName("登录成功返回 token")
    void login_returnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"loginuser\",\"password\":\"secret\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"loginuser\",\"password\":\"secret\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isNotEmpty())
            .andExpect(jsonPath("$.user.username").value("loginuser"));
    }

    @Test
    @DisplayName("错误密码登录返回 401")
    void login_wrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"wrongpassuser\",\"password\":\"correct\"}"))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"wrongpassuser\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("携带 token 访问 /me 返回当前用户")
    void me_withValidToken_returnsUser() throws Exception {
        String token = registerAndGetToken("meuser", "pass");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("meuser"))
            .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    @DisplayName("无 token 访问 /me 返回 401 或 403")
    void me_withoutToken_returns4xx() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(result -> {
                int status = result.getResponse().getStatus();
                if (status != 401 && status != 403) throw new AssertionError("Expected 401 or 403, got " + status);
            });
    }
}
