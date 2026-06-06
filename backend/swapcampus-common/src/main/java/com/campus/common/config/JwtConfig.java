package com.campus.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "swapcampus.jwt")
public class JwtConfig {

    private String secret;

    private Long expireSeconds;

    private String issuer;
}
