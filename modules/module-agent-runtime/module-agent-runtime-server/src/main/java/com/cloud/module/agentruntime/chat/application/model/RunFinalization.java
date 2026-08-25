package com.cloud.module.agentruntime.chat.application.model;

import com.cloud.module.agentruntime.chat.domain.AgentRunStatus;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class RunFinalization {

    private final Long sessionId;
    private final Long runId;
    private final AgentRunStatus status;

    /**
     * 成功时保存的完整助手回复。
     */
    private final String assistantContent;

    /**
     * 失败时使用的安全错误码。
     */
    private final String errorCode;

    /**
     * 失败时使用的已脱敏错误信息。
     */
    private final String errorMessage;

    private RunFinalization(
            Long sessionId,
            Long runId,
            AgentRunStatus status,
            String assistantContent,
            String errorCode,
            String errorMessage
    ) {
        this.sessionId = requirePositive(
                sessionId,
                "sessionId"
        );
        this.runId = requirePositive(runId, "runId");
        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );

        if (status == AgentRunStatus.RUNNING) {
            throw new IllegalArgumentException(
                    "finalization status must not be RUNNING"
            );
        }

        this.assistantContent = assistantContent;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static RunFinalization succeeded(
            Long sessionId,
            Long runId,
            String assistantContent
    ) {
        requireText(assistantContent, "assistantContent");

        return new RunFinalization(
                sessionId,
                runId,
                AgentRunStatus.SUCCEEDED,
                assistantContent,
                null,
                null
        );
    }

    public static RunFinalization failed(
            Long sessionId,
            Long runId,
            String errorCode,
            String errorMessage
    ) {
        requireText(errorCode, "errorCode");
        requireText(errorMessage, "errorMessage");

        return new RunFinalization(
                sessionId,
                runId,
                AgentRunStatus.FAILED,
                null,
                errorCode,
                errorMessage
        );
    }

    public static RunFinalization cancelled(
            Long sessionId,
            Long runId
    ) {
        return new RunFinalization(
                sessionId,
                runId,
                AgentRunStatus.CANCELLED,
                null,
                null,
                null
        );
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
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value;
    }
}