package com.cloud.module.agentruntime.chat.runtime;

import com.cloud.module.agentruntime.chat.domain.ChatActor;
import com.cloud.module.agentruntime.chat.domain.ChatMessage;
import com.cloud.module.agentruntime.chat.domain.ChatRole;
import lombok.Getter;

import java.util.List;
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
     * 传递给 Agent 的完整消息列表，包含当前用户输入。
     */
    private final List<ChatMessage> messages;

    public AgentInvocation(
            Long sessionId,
            Long runId,
            ChatActor actor,
            List<ChatMessage> messages
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

        Objects.requireNonNull(messages, "messages must not be null");

        if (messages.isEmpty()) {
            throw new IllegalArgumentException(
                    "messages must not be empty"
            );
        }

        this.messages = List.copyOf(messages);

        ChatMessage lastMessage =
                this.messages.get(this.messages.size() - 1);

        if (lastMessage.getRole() != ChatRole.USER) {
            throw new IllegalArgumentException(
                    "the last invocation message must be a user message"
            );
        }
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