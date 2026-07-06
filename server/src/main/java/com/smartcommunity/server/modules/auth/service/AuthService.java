package com.smartcommunity.server.modules.auth.service;

import com.smartcommunity.server.common.exception.BusinessException;
import com.smartcommunity.server.modules.auth.captcha.CaptchaService;
import com.smartcommunity.server.modules.auth.dto.LoginRequest;
import com.smartcommunity.server.modules.auth.vo.LoginResponse;
import com.smartcommunity.server.modules.auth.vo.MenuItemVO;
import com.smartcommunity.server.modules.auth.vo.UserProfileVO;
import com.smartcommunity.server.modules.system.entity.User;
import com.smartcommunity.server.modules.system.service.MenuService;
import com.smartcommunity.server.modules.system.service.RoleService;
import com.smartcommunity.server.modules.system.service.UserService;
import com.smartcommunity.server.security.JwtTokenProvider;
import com.smartcommunity.server.security.LoginUser;
import com.smartcommunity.server.security.SecurityContext;
import com.smartcommunity.server.security.TokenStoreService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStoreService tokenStoreService;
    private final CaptchaService captchaService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserService userService,
                       @Lazy RoleService roleService,
                       MenuService menuService,
                       JwtTokenProvider jwtTokenProvider,
                       TokenStoreService tokenStoreService,
                       CaptchaService captchaService) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenStoreService = tokenStoreService;
        this.captchaService = captchaService;
    }

    public LoginResponse login(LoginRequest request) {
        // 先校验验证码
        if (!captchaService.validate(request.captchaKey(), request.captchaCode())) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }

        User user = userService.getByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw BusinessException.badRequest("账号已被禁用");
        }

        // 查询角色和权限
        List<Long> roleIds = userService.getRoleIds(user.getUserId());
        Set<String> roleCodes = new LinkedHashSet<>();
        for (Long roleId : roleIds) {
            var role = roleService.getById(roleId);
            if (role != null) roleCodes.add(role.getRoleCode());
        }
        Set<String> permissions = new LinkedHashSet<>(menuService.getPermissionsByRoleIds(roleIds));
        List<MenuItemVO> menus = menuService.getMenuTreeByRoleIds(roleIds);

        LoginUser loginUser = new LoginUser(
                user.getUserId(), user.getUsername(), user.getRealName(),
                roleCodes, permissions
        );

        // 生成 JWT（仅含 userId + tokenId）+ Redis 存储完整会话
        JwtTokenProvider.TokenPair pair = jwtTokenProvider.generateToken(user.getUserId());
        Duration ttl = Duration.ofSeconds(jwtTokenProvider.getExpireSeconds());
        tokenStoreService.save(pair.tokenId(), loginUser, ttl);

        return new LoginResponse(
                pair.token(),
                toProfile(loginUser),
                permissions,
                menus
        );
    }

    /** 登出：删除 Redis 会话 */
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }
        try {
            String token = authorizationHeader.substring(7);
            String tokenId = jwtTokenProvider.parseTokenId(token);
            tokenStoreService.remove(tokenId);
        } catch (Exception ignored) {
            // Token 已过期或非法，不需要额外处理
        }
    }

    public UserProfileVO currentUser() {
        LoginUser loginUser = SecurityContext.get();
        if (loginUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return toProfile(loginUser);
    }

    private UserProfileVO toProfile(LoginUser loginUser) {
        return new UserProfileVO(
                loginUser.userId(), loginUser.username(), loginUser.realName(),
                loginUser.roles(), loginUser.permissions()
        );
    }
}
