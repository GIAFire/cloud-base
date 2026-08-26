package com.cloud.module.agent.chat.runtime;

import com.cloud.module.agent.chat.domain.ChatActor;
import lombok.Getter;

import java.util.Objects;

/**
 * 应用层提交给 AgentRuntime 的单次调用参数。
 *
 * <p>只携带当前用户输入，不携带业务消息历史。HarnessAgent 会使用 actor 与
 * sessionId 构造 RuntimeContext，并由 AgentScope StateStore 管理模型上下文。</p>
 */
@Getter
public final class AgentInvocation {

    /**
     * 会话ID，后续用于构造 AgentScope RuntimeContext。
     */
    private final Long sessionId;

    /**
     * 本次运行ID。
     */
    private final Long runId;

    /**
     * 当前用户和租户身份，同时参与 AgentScope 上下文隔离。
     */
    private final ChatActor actor;

    /**
     * 本次调用的当前用户输入；必须为非空白文本。
     */
    private final String content;

    public AgentInvocation(
            Long sessionId,
            Long runId,
            ChatActor actor,
            String content
    ) {
        this.sessionId = requirePositive(sessionId, "sessionId");
        this.runId = requirePositive(runId, "runId");
        this.actor = Objects.requireNonNull(actor, "actor must not be null");

        this.content = requireText(content, "content");
    }

    private static Long requirePositive(Long value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0");
        }

        return value;
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }
}
