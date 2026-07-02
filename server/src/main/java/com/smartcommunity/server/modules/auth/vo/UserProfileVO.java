package com.smartcommunity.server.modules.auth.vo;

import java.util.Set;

public record UserProfileVO(
        Long userId,
        String username,
        String realName,
        Set<String> roles,
        Set<String> permissions
) {
}
