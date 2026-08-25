package com.cloud.module.agentruntime.chat.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class ChatEvent {

    private static final int CURRENT_SCHEMA_VERSION = 1;

    private final int schemaVersion;
    private final Long sessionId;
    private final Long runId;
    private final long sequence;
    private final ChatEventType type;
    private final String delta;
    private final String errorCode;
    private final String errorMessage;

    private ChatEvent(
            Long sessionId,
            Long runId,
            long sequence,
            ChatEventType type,
            String delta,
            String errorCode,
            String errorMessage
    ) {
        this.schemaVersion = CURRENT_SCHEMA_VERSION;
        this.sessionId = requirePositive(sessionId, "sessionId");
        this.runId = requirePositive(runId, "runId");

        if (sequence <= 0) {
            throw new IllegalArgumentException("sequence must be greater than 0");
        }

        this.sequence = sequence;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.delta = delta;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static ChatEvent runStarted(
            Long sessionId,
            Long runId,
            long sequence
    ) {
        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.RUN_STARTED,
                null,
                null,
                null
        );
    }

    public static ChatEvent messageThinkingDelta(
            Long sessionId,
            Long runId,
            long sequence,
            String delta
    ) {
        if (delta == null || delta.isEmpty()) {
            throw new IllegalArgumentException(
                    "delta must not be null or empty"
            );
        }

        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.MESSAGE_THINKING_DELTA,
                delta,
                null,
                null
        );
    }

    public static ChatEvent messageDelta(
            Long sessionId,
            Long runId,
            long sequence,
            String delta
    ) {
        if (delta == null || delta.isEmpty()) {
            throw new IllegalArgumentException(
                    "delta must not be null or empty"
            );
        }

        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.MESSAGE_DELTA,
                delta,
                null,
                null
        );
    }

    public static ChatEvent runCompleted(
            Long sessionId,
            Long runId,
            long sequence
    ) {
        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.RUN_COMPLETED,
                null,
                null,
                null
        );
    }

    public static ChatEvent runFailed(
            Long sessionId,
            Long runId,
            long sequence,
            String errorCode,
            String errorMessage
    ) {
        requireText(errorCode, "errorCode");
        requireText(errorMessage, "errorMessage");

        return new ChatEvent(
                sessionId,
                runId,
                sequence,
                ChatEventType.RUN_FAILED,
                null,
                errorCode,
                errorMessage
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