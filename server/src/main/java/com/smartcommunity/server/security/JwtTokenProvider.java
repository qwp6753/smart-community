package com.smartcommunity.server.security;

import com.smartcommunity.server.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        byte[] raw = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32];
        for (int index = 0; index < keyBytes.length; index++) {
            keyBytes[index] = raw[index % raw.length];
        }
        String base64Secret = Base64.getEncoder().encodeToString(keyBytes);
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
    }

    public String generateToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(jwtProperties.expireSeconds());
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(loginUser.userId()))
                .claim("username", loginUser.username())
                .claim("realName", loginUser.realName())
                .claim("roles", loginUser.roles())
                .claim("permissions", loginUser.permissions())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }

    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new LoginUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("username", String.class),
                    claims.get("realName", String.class),
                    asStringSet(claims.get("roles", List.class)),
                    asStringSet(claims.get("permissions", List.class))
            );
        } catch (Exception exception) {
            throw BusinessException.unauthorized("登录状态已失效，请重新登录");
        }
    }

    private Set<String> asStringSet(List<?> source) {
        Set<String> values = new LinkedHashSet<>();
        if (source == null) {
            return values;
        }
        for (Object item : source) {
            values.add(String.valueOf(item));
        }
        return values;
    }
}
