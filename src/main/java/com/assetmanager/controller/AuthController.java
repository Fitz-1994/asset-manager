package com.assetmanager.controller;

import com.assetmanager.dto.TokenResponse;
import com.assetmanager.dto.UserCreate;
import com.assetmanager.dto.UserResponse;
import com.assetmanager.entity.User;
import com.assetmanager.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public TokenResponse register(@Valid @RequestBody UserCreate body) {
        log.info("注册请求: username={}", body.getUsername() != null ? body.getUsername() : "(null)");
        try {
            TokenResponse res = authService.register(body);
            log.info("注册成功: username={}", body.getUsername());
            return res;
        } catch (Exception e) {
            log.warn("注册失败: username={}, error={}", body.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody UserCreate body) {
        return authService.login(body);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        if (user == null)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Not authenticated");
        return UserResponse.from(user.getId(), user.getUsername(), user.getCreatedAt());
    }
}
