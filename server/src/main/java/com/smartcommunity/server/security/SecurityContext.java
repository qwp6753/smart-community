package com.smartcommunity.server.security;

public final class SecurityContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
