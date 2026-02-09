package com.assetmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.assetmanager.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class AssetManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetManagerApplication.class, args);
    }
}
