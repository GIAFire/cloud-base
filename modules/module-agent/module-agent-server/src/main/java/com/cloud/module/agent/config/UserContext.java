package com.cloud.module.agent.config;


import com.alibaba.ttl.TransmittableThreadLocal;
import com.cloud.framework.security.core.LoginUser;

import java.util.function.Supplier;

public class UserContext {

    private static final ThreadLocal<LoginUser> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

//    private static final ThreadLocal<LoginUser> USER_THREAD_LOCAL = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        USER_THREAD_LOCAL.set(loginUser);
    }

    public static LoginUser get() {
        return USER_THREAD_LOCAL.get();
    }

    public static Long getUserId() {
        LoginUser loginUser = get();
        return loginUser == null ? null : loginUser.getId();
    }

    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }

    public static void runAs(LoginUser loginUser, Runnable runnable) {
        LoginUser previous = get();
        try {
            set(loginUser);
            runnable.run();
        } finally {
            restore(previous);
        }
    }

    public static <T> T callAs(LoginUser loginUser, Supplier<T> supplier) {
        LoginUser previous = get();
        try {
            set(loginUser);
            return supplier.get();
        } finally {
            restore(previous);
        }
    }

    private static void restore(LoginUser previous) {
        if (previous == null) {
            clear();
            return;
        }
        set(previous);
    }
}
