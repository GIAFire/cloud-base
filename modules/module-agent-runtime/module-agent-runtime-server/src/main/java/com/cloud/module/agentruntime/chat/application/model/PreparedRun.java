package com.cloud.module.agentruntime.chat.application.model;

import com.cloud.module.agentruntime.chat.domain.ChatMessage;
import com.cloud.module.agentruntime.chat.domain.ChatRole;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public final class PreparedRun {

    /**
     * 本次运行所属会话。
     */
    private final Long sessionId;

    /**
     * 后端为本次 Agent 执行生成的运行ID。
     */
    private final Long runId;

    /**
     * 完整消息历史，已经包含本次用户消息。
     */
    private final List<ChatMessage> history;

    public PreparedRun(
            Long sessionId,
            Long runId,
            List<ChatMessage> history
    ) {
        this.sessionId = requirePositive(sessionId,"sessionId");
        this.runId = requirePositive(runId, "runId");

        Objects.requireNonNull(history, "history must not be null");

        if (history.isEmpty()) {
            throw new IllegalArgumentException(
                    "history must not be empty"
            );
        }

        this.history = List.copyOf(history);

        ChatMessage lastMessage = this.history.get(this.history.size() - 1);

        if (lastMessage.getRole() != ChatRole.USER) {
            throw new IllegalArgumentException(
                    "the last history message must be a user message"
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