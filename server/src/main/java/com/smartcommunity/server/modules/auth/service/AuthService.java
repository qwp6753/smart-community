package com.smartcommunity.server.modules.auth.service;

import com.smartcommunity.server.common.exception.BusinessException;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserService userService,
                       @Lazy RoleService roleService,
                       MenuService menuService,
                       JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService.getByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw BusinessException.badRequest("账号已被禁用");
        }

        // 查询用户角色ID列表
        List<Long> roleIds = userService.getRoleIds(user.getUserId());

        // 查询角色编码
        Set<String> roleCodes = new LinkedHashSet<>();
        for (Long roleId : roleIds) {
            var role = roleService.getById(roleId);
            if (role != null) {
                roleCodes.add(role.getRoleCode());
            }
        }

        // 查询权限标识和菜单树
        Set<String> permissions = new LinkedHashSet<>(menuService.getPermissionsByRoleIds(roleIds));
        List<MenuItemVO> menus = menuService.getMenuTreeByRoleIds(roleIds);

        LoginUser loginUser = new LoginUser(
                user.getUserId(),
                user.getUsername(),
                user.getRealName(),
                roleCodes,
                permissions
        );

        String token = jwtTokenProvider.generateToken(loginUser);
        return new LoginResponse(
                token,
                toProfile(loginUser),
                permissions,
                menus
        );
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
                loginUser.userId(),
                loginUser.username(),
                loginUser.realName(),
                loginUser.roles(),
                loginUser.permissions()
        );
    }
}
