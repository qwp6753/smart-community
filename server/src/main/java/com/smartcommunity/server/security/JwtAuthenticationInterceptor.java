package com.smartcommunity.server.security;

import com.smartcommunity.server.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * JWT + Redis 鉴权拦截器。
 * 流程：Header 取 Bearer token → JWT 解析 tokenId → Redis 读取 LoginUser → 写入 SecurityContext
 */
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStoreService tokenStoreService;

    public JwtAuthenticationInterceptor(JwtTokenProvider jwtTokenProvider,
                                        TokenStoreService tokenStoreService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenStoreService = tokenStoreService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("请先登录");
        }

        String token = authorization.substring(7);
        String tokenId = jwtTokenProvider.parseTokenId(token);

        // 从 Redis 读取会话并滑动续期
        Duration ttl = Duration.ofSeconds(jwtTokenProvider.getExpireSeconds());
        LoginUser loginUser = tokenStoreService.getAndRefresh(tokenId, ttl);
        if (loginUser == null) {
            throw BusinessException.unauthorized("登录状态已失效，请重新登录");
        }

        SecurityContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        SecurityContext.clear();
    }
}
