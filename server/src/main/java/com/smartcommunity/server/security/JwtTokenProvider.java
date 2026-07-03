package com.smartcommunity.server.security;

import com.smartcommunity.server.common.exception.BusinessException;
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
import java.util.UUID;

/**
 * JWT 令牌工具。
 * 设计原则：JWT 中仅存储 userId + tokenId，完整权限/角色信息存 Redis，
 * 这样即使 JWT 被截获也无法获取权限列表；登出时只需删除 Redis key。
 */
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
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = raw[i % raw.length];
        }
        String base64 = Base64.getEncoder().encodeToString(keyBytes);
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64));
    }

    public long getExpireSeconds() {
        return jwtProperties.expireSeconds();
    }

    /**
     * 生成 JWT，payload 只存 userId + tokenId。
     * @return { token, tokenId } — tokenId 作为 Redis 会话 key
     */
    public TokenPair generateToken(Long userId) {
        String tokenId = UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(jwtProperties.expireSeconds());

        String token = Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(userId))
                .id(tokenId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();

        return new TokenPair(token, tokenId);
    }

    /**
     * 解析 JWT 返回 tokenId（仅校验签名和有效期，不返回用户信息）
     */
    public String parseTokenId(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getId();
        } catch (Exception e) {
            throw BusinessException.unauthorized("登录状态已失效，请重新登录");
        }
    }

    public record TokenPair(String token, String tokenId) {
    }
}
