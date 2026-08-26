package com.cloud.module.agentruntime.chat.application.model;

import com.cloud.module.agentruntime.chat.domain.AgentRunStatus;
import com.cloud.module.agentruntime.chat.domain.ChatActor;
import lombok.Getter;

import java.util.Objects;

@Getter
public final class RunFinalization {

    private final Long sessionId;

    private final Long runId;

    private final ChatActor actor;

    private final AgentRunStatus status;

    private final String assistantContent;

    private final String errorCode;

    private final String errorMessage;

    private RunFinalization(
            Long sessionId,
            Long runId,
            ChatActor actor,
            AgentRunStatus status,
            String assistantContent,
            String errorCode,
            String errorMessage
    ) {
        this.sessionId = requirePositive(
                sessionId,
                "sessionId"
        );
        this.runId = requirePositive(
                runId,
                "runId"
        );
        this.actor = Objects.requireNonNull(
                actor,
                "actor must not be null"
        );
        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );

        if (status == AgentRunStatus.RUNNING) {
            throw new IllegalArgumentException(
                    "finalization status must not be RUNNING"
            );
        }

        validateContent(
                status,
                assistantContent,
                errorCode,
                errorMessage
        );

        this.assistantContent = assistantContent;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static RunFinalization succeeded(
            Long sessionId,
            Long runId,
            ChatActor actor,
            String assistantContent
    ) {
        return new RunFinalization(
                sessionId,
                runId,
                actor,
                AgentRunStatus.SUCCEEDED,
                assistantContent,
                null,
                null
        );
    }

    public static RunFinalization failed(
            Long sessionId,
            Long runId,
            ChatActor actor,
            String errorCode,
            String errorMessage
    ) {
        return new RunFinalization(
                sessionId,
                runId,
                actor,
                AgentRunStatus.FAILED,
                null,
                errorCode,
                errorMessage
        );
    }

    public static RunFinalization cancelled(
            Long sessionId,
            Long runId,
            ChatActor actor
    ) {
        return new RunFinalization(
                sessionId,
                runId,
                actor,
                AgentRunStatus.CANCELLED,
                null,
                null,
                null
        );
    }

    private static void validateContent(
            AgentRunStatus status,
            String assistantContent,
            String errorCode,
            String errorMessage
    ) {
        switch (status) {
            case SUCCEEDED -> {
                requireText(
                        assistantContent,
                        "assistantContent"
                );

                if (errorCode != null
                        || errorMessage != null) {
                    throw new IllegalArgumentException(
                            "successful finalization "
                                    + "must not contain an error"
                    );
                }
            }

            case FAILED -> {
                requireText(
                        errorCode,
                        "errorCode"
                );
                requireText(
                        errorMessage,
                        "errorMessage"
                );

                if (assistantContent != null) {
                    throw new IllegalArgumentException(
                            "failed finalization "
                                    + "must not contain assistantContent"
                    );
                }
            }

            case CANCELLED -> {
                if (assistantContent != null
                        || errorCode != null
                        || errorMessage != null) {
                    throw new IllegalArgumentException(
                            "cancelled finalization "
                                    + "must not contain result content"
                    );
                }
            }

            case RUNNING -> throw
                    new IllegalArgumentException(
                            "RUNNING is not a final status"
                    );
        }
    }

    private static Long requirePositive(
            Long value,
            String fieldName
    ) {
        Objects.requireNonNull(
                value,
                fieldName + " must not be null"
        );

        if (value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than 0"
            );
        }

        return value;
    }

    private static void requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }
    }
}