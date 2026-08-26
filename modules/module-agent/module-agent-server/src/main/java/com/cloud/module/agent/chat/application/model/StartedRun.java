package com.cloud.module.agent.chat.application.model;

import lombok.Getter;

import java.util.Objects;

/**
 * ChatStore 完成会话解析和 Run 创建后返回的运行标识。
 *
 * <p>Agent 对话上下文由 AgentScope StateStore 负责，因此这里不携带聊天历史。</p>
 */
@Getter
public final class StartedRun {

    private final Long sessionId;

    private final Long runId;

    public StartedRun(Long sessionId, Long runId) {
        this.sessionId = requirePositive(sessionId, "sessionId");
        this.runId = requirePositive(runId, "runId");
    }

    private static Long requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than 0"
            );
        }

        return value;
    }
}
