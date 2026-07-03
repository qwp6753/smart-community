package com.smartcommunity.server.modules.auth.controller;

import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.auth.dto.LoginRequest;
import com.smartcommunity.server.modules.auth.service.AuthService;
import com.smartcommunity.server.modules.auth.vo.LoginResponse;
import com.smartcommunity.server.modules.auth.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("Authorization"));
        return ApiResponse.success("已退出登录", null);
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> currentUser() {
        return ApiResponse.success(authService.currentUser());
    }
}
