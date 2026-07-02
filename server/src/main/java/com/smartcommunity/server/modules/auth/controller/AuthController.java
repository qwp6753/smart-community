package com.smartcommunity.server.modules.auth.controller;

import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.auth.dto.LoginRequest;
import com.smartcommunity.server.modules.auth.service.AuthService;
import com.smartcommunity.server.modules.auth.vo.LoginResponse;
import com.smartcommunity.server.modules.auth.vo.UserProfileVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/me")
    public ApiResponse<UserProfileVO> currentUser() {
        return ApiResponse.success(authService.currentUser());
    }
}
