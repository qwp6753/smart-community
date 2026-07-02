package com.smartcommunity.server.security;

import java.util.Set;

public record LoginUser(
        Long userId,
        String username,
        String realName,
        Set<String> roles,
        Set<String> permissions
) {
}
