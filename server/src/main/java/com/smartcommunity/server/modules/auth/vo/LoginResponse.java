package com.smartcommunity.server.modules.auth.vo;

import java.util.List;
import java.util.Set;

public record LoginResponse(
        String token,
        UserProfileVO userInfo,
        Set<String> permissions,
        List<MenuItemVO> menus
) {
}
