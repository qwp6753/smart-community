package com.smartcommunity.server.modules.auth.controller;

import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.auth.captcha.CaptchaService;
import com.smartcommunity.server.modules.auth.dto.LoginRequest;
import com.smartcommunity.server.modules.auth.service.AuthService;
import com.smartcommunity.server.modules.auth.vo.LoginResponse;
import com.smartcommunity.server.modules.auth.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    public AuthController(AuthService authService, CaptchaService captchaService) {
        this.authService = authService;
        this.captchaService = captchaService;
    }

    /** 获取图片验证码 */
    @GetMapping("/captcha")
    public ApiResponse<Map<String, String>> captcha() {
        CaptchaService.CaptchaResult result = captchaService.generate();
        return ApiResponse.success(Map.of(
                "captchaKey", result.captchaKey(),
                "captchaImage", result.captchaImage()
        ));
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
