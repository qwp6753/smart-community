package com.smartcommunity.server.modules.auth.service;

import com.smartcommunity.server.common.exception.BusinessException;
import com.smartcommunity.server.modules.auth.dto.LoginRequest;
import com.smartcommunity.server.modules.auth.vo.LoginResponse;
import com.smartcommunity.server.modules.auth.vo.MenuItemVO;
import com.smartcommunity.server.modules.auth.vo.UserProfileVO;
import com.smartcommunity.server.security.JwtTokenProvider;
import com.smartcommunity.server.security.LoginUser;
import com.smartcommunity.server.security.SecurityContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private static final Long ADMIN_USER_ID = 1L;
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_REAL_NAME = "系统管理员";
    private static final String ADMIN_PASSWORD_HASH = "$2a$10$uIhq8JhLT.PgQ57/bV5AIuzG6pBAVD7Vj2Wyl.DymUB3QhxUKLkxm";

    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN");
    private static final Set<String> ADMIN_PERMISSIONS = Set.of(
            "dashboard:view",
            "system:user:view",
            "system:user:edit",
            "system:role:view",
            "system:role:edit",
            "system:menu:view",
            "property:community:view",
            "property:community:edit",
            "property:camera:view",
            "property:camera:edit",
            "property:person:view",
            "property:person:edit",
            "access:record:view",
            "access:visitor:view",
            "access:visitor:edit"
    );

    private final JwtTokenProvider jwtTokenProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        if (!ADMIN_USERNAME.equals(request.username()) || !passwordEncoder.matches(request.password(), ADMIN_PASSWORD_HASH)) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        LoginUser loginUser = new LoginUser(
                ADMIN_USER_ID,
                ADMIN_USERNAME,
                ADMIN_REAL_NAME,
                ADMIN_ROLES,
                ADMIN_PERMISSIONS
        );
        String token = jwtTokenProvider.generateToken(loginUser);
        return new LoginResponse(
                token,
                toProfile(loginUser),
                new LinkedHashSet<>(ADMIN_PERMISSIONS),
                buildMenus()
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

    private List<MenuItemVO> buildMenus() {
        return List.of(
                MenuItemVO.builder()
                        .name("仪表盘")
                        .path("/dashboard")
                        .component("dashboard/index")
                        .icon("Odometer")
                        .permission("dashboard:view")
                        .children(List.of())
                        .build(),
                MenuItemVO.builder()
                        .name("系统管理")
                        .path("/system")
                        .component("Layout")
                        .icon("Setting")
                        .permission("system:user:view")
                        .children(List.of(
                                MenuItemVO.builder().name("用户管理").path("/system/users").component("system/user/index").icon("User").permission("system:user:view").children(List.of()).build(),
                                MenuItemVO.builder().name("角色管理").path("/system/roles").component("system/role/index").icon("UserFilled").permission("system:role:view").children(List.of()).build(),
                                MenuItemVO.builder().name("菜单管理").path("/system/menus").component("system/menu/index").icon("Menu").permission("system:menu:view").children(List.of()).build()
                        ))
                        .build(),
                MenuItemVO.builder()
                        .name("物业管理")
                        .path("/property")
                        .component("Layout")
                        .icon("OfficeBuilding")
                        .permission("property:community:view")
                        .children(List.of(
                                MenuItemVO.builder().name("小区管理").path("/property/communities").component("property/community/index").icon("House").permission("property:community:view").children(List.of()).build(),
                                MenuItemVO.builder().name("摄像头管理").path("/property/cameras").component("property/camera/index").icon("VideoCamera").permission("property:camera:view").children(List.of()).build(),
                                MenuItemVO.builder().name("居民管理").path("/property/persons").component("property/person/index").icon("Avatar").permission("property:person:view").children(List.of()).build()
                        ))
                        .build(),
                MenuItemVO.builder()
                        .name("门禁管理")
                        .path("/access")
                        .component("Layout")
                        .icon("Lock")
                        .permission("access:record:view")
                        .children(List.of(
                                MenuItemVO.builder().name("出入记录").path("/access/records").component("access/record/index").icon("Tickets").permission("access:record:view").children(List.of()).build(),
                                MenuItemVO.builder().name("访客登记").path("/access/visitors").component("access/visitor/index").icon("Notebook").permission("access:visitor:view").children(List.of()).build()
                        ))
                        .build()
        );
    }
}
