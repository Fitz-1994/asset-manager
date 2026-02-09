package com.assetmanager.service;

import com.assetmanager.config.JwtProperties;
import com.assetmanager.dto.TokenResponse;
import com.assetmanager.dto.UserCreate;
import com.assetmanager.dto.UserResponse;
import com.assetmanager.entity.User;
import com.assetmanager.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtProperties jwtProperties) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtProperties = jwtProperties;
    }

    public TokenResponse register(UserCreate dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名不能为空");
        if (dto.getPassword() == null || dto.getPassword().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码不能为空");
        if (userMapper.existsByUsername(dto.getUsername().trim()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already registered");

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(Instant.now());
        userMapper.insert(user);

        String token = createToken(user.getId());
        TokenResponse res = new TokenResponse();
        res.setAccess_token(token);
        res.setUser(UserResponse.from(user.getId(), user.getUsername(), user.getCreatedAt()));
        return res;
    }

    public TokenResponse login(UserCreate dto) {
        User user = userMapper.findByUsername(dto.getUsername() != null ? dto.getUsername().trim() : null);
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");

        String token = createToken(user.getId());
        TokenResponse res = new TokenResponse();
        res.setAccess_token(token);
        res.setUser(UserResponse.from(user.getId(), user.getUsername(), user.getCreatedAt()));
        return res;
    }

    private String createToken(Long userId) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
            .signWith(key)
            .compact();
    }
}
