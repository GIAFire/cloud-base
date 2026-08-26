package com.cloud.module.agent.chat.runtime;

import com.cloud.module.agent.chat.domain.ChatActor;
import lombok.Getter;

import java.util.Objects;

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
     * 当前用户和租户身份。
     */
    private final ChatActor actor;

    /**
     * 本次调用的当前用户输入。
     */
    private final String content;

    public AgentInvocation(
            Long sessionId,
            Long runId,
            ChatActor actor,
            String content
    ) {
        this.sessionId = requirePositive(
                sessionId,
                "sessionId"
        );
        this.runId = requirePositive(runId, "runId");
        this.actor = Objects.requireNonNull(
                actor,
                "actor must not be null"
        );

        this.content = requireText(content, "content");
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

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value;
    }
}
