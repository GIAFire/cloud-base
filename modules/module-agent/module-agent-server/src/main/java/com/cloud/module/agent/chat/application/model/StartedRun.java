package com.cloud.module.agent.chat.application.model;

import lombok.Getter;

import java.util.Objects;

/**
 * ChatStore 完成会话解析、Run 创建和用户消息持久化后返回的运行标识。
 *
 * <p>Agent 对话上下文由 AgentScope StateStore 负责，因此这里不携带聊天历史。</p>
 * <p>该对象返回给应用层时，对应 Run 已处于 RUNNING 状态，可以安全启动
 * AgentRuntime。</p>
 */
@Getter
public final class StartedRun {

    private final Long sessionId;

    private final Long runId;

    /** 创建 ChatStore begin 阶段的不可变结果。 */
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
