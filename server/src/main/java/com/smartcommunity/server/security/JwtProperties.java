package com.smartcommunity.server.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        Long expireSeconds
) {
}
