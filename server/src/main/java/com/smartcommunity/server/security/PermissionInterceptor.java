package com.smartcommunity.server.security;

import com.smartcommunity.server.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RequirePermission requirePermission = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequirePermission.class);
        }
        if (requirePermission == null) {
            return true;
        }

        LoginUser loginUser = SecurityContext.get();
        if (loginUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }

        Set<String> permissions = loginUser.permissions();
        for (String permission : requirePermission.value()) {
            if (permissions.contains(permission)) {
                return true;
            }
        }
        throw BusinessException.forbidden("没有访问权限");
    }
}
