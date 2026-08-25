package com.cloud.module.agentruntime.chat.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class ChatActor {

    private final Long tenantId;
    private final Long userId;

    public ChatActor(Long tenantId, Long userId) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
    }
}
