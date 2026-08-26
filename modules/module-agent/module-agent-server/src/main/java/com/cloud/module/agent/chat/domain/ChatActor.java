package com.cloud.module.agent.chat.domain;

import lombok.Getter;

import java.util.Objects;

/**
 * 当前聊天参与者的业务身份。
 *
 * <p>租户和用户必须同时参与 Session、Run、requestId 以及 AgentScope
 * RuntimeContext 的隔离，不能只依赖全局 ThreadLocal。</p>
 */
@Getter
public final class ChatActor {

    /** 多租户隔离标识。 */
    private final Long tenantId;

    /** 当前登录用户标识。 */
    private final Long userId;

    /** 创建经过正数校验的身份值对象。 */
    public ChatActor(Long tenantId, Long userId) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        if (tenantId != null && tenantId <= 0) {
            throw new IllegalArgumentException("tenantId must be greater than 0");
        }
        if (userId != null && userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
    }
}
